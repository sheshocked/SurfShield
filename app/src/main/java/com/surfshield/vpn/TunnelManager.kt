package com.surfshield.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import android.util.Log
import com.surfshield.data.AppSettings
import com.surfshield.data.SurfLocation
import com.surfshield.net.EndpointProber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.amnezia.awg.backend.Backend
import org.amnezia.awg.backend.GoBackend
import org.amnezia.awg.backend.Tunnel
import org.amnezia.awg.config.Config
import java.io.ByteArrayInputStream

/**
 * Owns the tunnel lifecycle.
 *
 * The connect flow is deliberately a search rather than a single attempt,
 * because on a filtered network you cannot know in advance which endpoint IP is
 * still routable or how much obfuscation the DPI requires:
 *
 *   1. Rank candidate IPs by measured RTT (Smart Connect).
 *   2. For each candidate, walk the obfuscation ladder, best-known first.
 *   3. Bring the tunnel up and wait for a real handshake.
 *   4. On success, remember the winning profile for this network.
 *
 * A handshake is the only honest success signal. setState() returning without
 * throwing just means the interface was created.
 */
class TunnelManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SurfShield/Tunnel"
        private const val TUNNEL_NAME = "surfshield"

        /** How long to wait for the first handshake before trying the next profile. */
        private const val HANDSHAKE_TIMEOUT_MS = 15_000L
        private const val HANDSHAKE_POLL_MS = 350L

        @Volatile private var instance: TunnelManager? = null

        fun get(context: Context): TunnelManager =
            instance ?: synchronized(this) {
                instance ?: TunnelManager(context.applicationContext).also { instance = it }
            }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val settings = AppSettings.get(context)
    private val autoTuner = AutoTuner(context)

    private val backend: Backend by lazy { GoBackend(context) }

    private val stateFlow = MutableStateFlow(TunnelState())
    val state: StateFlow<TunnelState> = stateFlow.asStateFlow()

    private var connectJob: Job? = null
    private var statsJob: Job? = null

    private val tunnel = object : Tunnel {
        override fun getName() = TUNNEL_NAME
        override fun onStateChange(newState: Tunnel.State) {
            if (newState == Tunnel.State.DOWN && stateFlow.value.status == ConnectionStatus.CONNECTED) {
                stateFlow.value = TunnelState(
                    status = ConnectionStatus.DISCONNECTED,
                    location = stateFlow.value.location,
                )
            }
        }
    }

    // ------------------------------------------------------------------ public API

    fun connect(location: SurfLocation, allLocations: List<SurfLocation> = emptyList()) {
        connectJob?.cancel()
        connectJob = scope.launch { runConnect(location, allLocations) }
    }

    fun disconnect() {
        connectJob?.cancel()
        statsJob?.cancel()
        scope.launch {
            runCatching { backend.setState(tunnel, Tunnel.State.DOWN, null) }
                .onFailure { Log.w(TAG, "Failed to bring tunnel down", it) }
            stateFlow.value = TunnelState(location = stateFlow.value.location)
        }
    }

    fun toggle(location: SurfLocation, allLocations: List<SurfLocation> = emptyList()) {
        if (stateFlow.value.isActive) disconnect() else connect(location, allLocations)
    }

    /** Forget every learned profile, e.g. after the user changes ISP. */
    fun resetLearnedProfiles() = autoTuner.clear()

    // ------------------------------------------------------------- connect pipeline

    private suspend fun runConnect(requested: SurfLocation, allLocations: List<SurfLocation>) {
        val fingerprint = networkFingerprint()

        stateFlow.value = TunnelState(
            status = ConnectionStatus.CONNECTING,
            location = requested,
            detail = "Preparing",
        )

        val candidates = buildCandidates(requested, allLocations)
        if (candidates.isEmpty()) {
            fail("No reachable endpoint for ${requested.displayName}")
            return
        }

        for ((location, endpoint, rtt) in candidates) {
            val supportsAwg = location.serverSupportsAwg
            val plan = autoTuner.plan("$fingerprint|${location.id}", supportsAwg)

            for (profile in plan) {
                stateFlow.value = stateFlow.value.copy(
                    status = ConnectionStatus.TESTING,
                    location = location,
                    endpoint = endpoint,
                    profile = profile,
                    rttMs = rtt.takeIf { it != EndpointProber.UNREACHABLE },
                    detail = "Trying ${profile.label} via ${endpoint.substringBeforeLast(':')}",
                )

                val built = AwgConfigBuilder.plan(
                    location = location,
                    settings = settings,
                    endpoint = endpoint,
                    serverSupportsAwg = supportsAwg,
                    autoProfile = profile,
                )
                val text = AwgConfigBuilder.render(built, settings)

                if (bringUp(text) && awaitHandshake()) {
                    autoTuner.remember("$fingerprint|${location.id}", profile)
                    settings.lastLocationId = location.id
                    stateFlow.value = stateFlow.value.copy(
                        status = ConnectionStatus.CONNECTED,
                        connectedSinceMs = System.currentTimeMillis(),
                        detail = null,
                        error = null,
                    )
                    startStatsPolling()
                    return
                }

                runCatching { backend.setState(tunnel, Tunnel.State.DOWN, null) }
            }
        }

        fail("Could not establish a tunnel. Every endpoint and profile was tried.")
    }

    /**
     * Endpoint candidates, best first. With Smart Connect off we still keep the
     * location's own alternate IPs as fallbacks, because a single blackholed IP
     * is the most common failure and retrying it forever is pointless.
     */
    private suspend fun buildCandidates(
        requested: SurfLocation,
        allLocations: List<SurfLocation>,
    ): List<Triple<SurfLocation, String, Int>> {
        val pool = if (settings.smartConnect && allLocations.isNotEmpty()) {
            // Keep the requested location first, then nearby alternatives.
            listOf(requested) + allLocations.filter { it.id != requested.id }.take(4)
        } else {
            listOf(requested)
        }

        val endpoints = pool.flatMap { loc -> loc.candidateEndpoints().map { loc to it } }
        val ranked = EndpointProber.rank(endpoints.map { it.second })
        val rttByEndpoint = ranked.associate { it.endpoint to it.rttMs }

        return endpoints
            .map { (loc, ep) -> Triple(loc, ep, rttByEndpoint[ep] ?: EndpointProber.UNREACHABLE) }
            .sortedWith(
                compareBy(
                    // requested location always wins ties
                    { if (it.first.id == requested.id) 0 else 1 },
                    { it.third },
                )
            )
            .take(6)
    }

    private suspend fun bringUp(configText: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val config = Config.parse(ByteArrayInputStream(configText.toByteArray()))
            backend.setState(tunnel, Tunnel.State.UP, config)
            true
        }.getOrElse {
            Log.w(TAG, "setState UP failed", it)
            false
        }
    }

    /**
     * Polls the backend statistics for a completed handshake. A tunnel that
     * never handshakes reports no peer traffic, which is precisely the silent
     * failure the old implementation could not detect.
     */
    private suspend fun awaitHandshake(): Boolean {
        val deadline = System.currentTimeMillis() + HANDSHAKE_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val stats = runCatching { backend.getStatistics(tunnel) }.getOrNull()
            if (stats != null && (stats.totalRx() > 0 || stats.totalTx() > 0)) {
                // Any received byte proves the peer answered.
                if (stats.totalRx() > 0) return true
            }
            delay(HANDSHAKE_POLL_MS)
        }
        return false
    }

    private fun startStatsPolling() {
        statsJob?.cancel()
        statsJob = scope.launch {
            var lastRx = 0L
            var lastTx = 0L
            var lastAt = System.currentTimeMillis()
            while (true) {
                delay(1_000)
                val stats = runCatching { backend.getStatistics(tunnel) }.getOrNull() ?: continue
                val now = System.currentTimeMillis()
                val elapsed = (now - lastAt).coerceAtLeast(1)
                val rx = stats.totalRx()
                val tx = stats.totalTx()
                stateFlow.value = stateFlow.value.copy(
                    rxBytes = rx,
                    txBytes = tx,
                    rxRateBps = ((rx - lastRx) * 1_000 / elapsed).coerceAtLeast(0),
                    txRateBps = ((tx - lastTx) * 1_000 / elapsed).coerceAtLeast(0),
                )
                lastRx = rx
                lastTx = tx
                lastAt = now
            }
        }
    }

    private fun fail(message: String) {
        stateFlow.value = stateFlow.value.copy(
            status = ConnectionStatus.FAILED,
            detail = null,
            error = message,
        )
    }

    // ------------------------------------------------------------- fingerprinting

    /**
     * Coarse label for "the network I am on right now". Coarse on purpose: it
     * must survive DHCP churn while still telling home broadband apart from
     * mobile data, which behave very differently under filtering.
     */
    private fun networkFingerprint(): String {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return "unknown"
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return "offline"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val tm = context.getSystemService(TelephonyManager::class.java)
                val operator = runCatching { tm?.simOperator }.getOrNull().orEmpty()
                "cell:$operator"
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            else -> "other"
        }
    }
}
