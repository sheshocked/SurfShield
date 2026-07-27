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
 * The connect flow is a search rather than a single attempt, because on a
 * filtered network you cannot know in advance which endpoint IP is still
 * routable or how much obfuscation the DPI requires:
 *
 *   1. Rank candidate IPs by measured RTT.
 *   2. For each candidate, walk the obfuscation ladder, best-known first.
 *   3. Bring the tunnel up and wait for a real handshake.
 *   4. On success, remember the winning profile for this network.
 *
 * A handshake is the only honest success signal: setState() returning without
 * throwing means the interface was created, nothing more.
 *
 * Every failure carries a reason. An earlier version caught and discarded them,
 * which produced a single useless "everything was tried" message no matter
 * whether the native library was missing, consent had been denied, or the
 * endpoint was simply blackholed. Those need very different fixes, so they are
 * classified and reported.
 */
class TunnelManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "SurfShield/Tunnel"
        private const val TUNNEL_NAME = "surfshield"

        /**
         * How long to wait for the first handshake before moving on.
         *
         * Deliberately generous: a junk-padded handshake to a European endpoint
         * over a throttled Iranian link can take well past seven seconds, and a
         * premature give-up looks exactly like a blocked endpoint.
         */
        private const val HANDSHAKE_TIMEOUT_MS = 12_000L
        private const val HANDSHAKE_POLL_MS = 300L

        /** Attempt matrix caps. Endpoints times profiles times timeout is wall-clock. */
        private const val MAX_ENDPOINTS = 3
        private const val MAX_PROFILES = 3

        @Volatile private var instance: TunnelManager? = null

        fun get(context: Context): TunnelManager =
            instance ?: synchronized(this) {
                instance ?: TunnelManager(context.applicationContext).also { instance = it }
            }
    }

    /** Why a single attempt failed. Distinct cases need distinct user advice. */
    private sealed interface Failure {
        val message: String

        /** libwg-go.so absent: the tunnel module was not built into the APK. */
        data class NativeMissing(override val message: String) : Failure

        /** The backend refused the generated config. A bug on our side. */
        data class BadConfig(override val message: String) : Failure

        /** VpnService consent missing or revoked. */
        data class NoPermission(override val message: String) : Failure

        /** Interface came up, peer never answered. Endpoint blocked or wrong port. */
        data class NoHandshake(override val message: String) : Failure

        data class Other(override val message: String) : Failure
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

    /** Forget every learned profile, e.g. after changing ISP. */
    fun resetLearnedProfiles() = autoTuner.clear()

    // ------------------------------------------------------------- connect pipeline

    private suspend fun runConnect(requested: SurfLocation, allLocations: List<SurfLocation>) {
        val fingerprint = networkFingerprint()

        stateFlow.value = TunnelState(
            status = ConnectionStatus.CONNECTING,
            location = requested,
            detail = "Preparing",
        )

        if (fingerprint == "offline") {
            fail("This device has no network connection.")
            return
        }

        val candidates = buildCandidates(requested, allLocations)
        if (candidates.isEmpty()) {
            fail(
                "No endpoint to try for ${requested.displayName}. The profile has no " +
                    "usable IP address."
            )
            return
        }

        var attempts = 0
        var lastFailure: Failure? = null

        for ((location, endpoint, rtt) in candidates) {
            val supportsAwg = location.serverSupportsAwg
            val plan = autoTuner.plan("$fingerprint|${location.id}", supportsAwg)
                .take(MAX_PROFILES)

            for (profile in plan) {
                attempts++
                stateFlow.value = stateFlow.value.copy(
                    status = ConnectionStatus.TESTING,
                    location = location,
                    endpoint = endpoint,
                    profile = profile,
                    rttMs = rtt.takeIf { it != EndpointProber.UNREACHABLE },
                    detail = "Attempt $attempts: ${profile.label} via ${endpoint.substringBeforeLast(':')}",
                    error = null,
                )

                val built = AwgConfigBuilder.plan(
                    location = location,
                    settings = settings,
                    endpoint = endpoint,
                    serverSupportsAwg = supportsAwg,
                    autoProfile = profile,
                )
                val text = AwgConfigBuilder.render(built, settings)

                if (settings.verboseLogging) {
                    // Redacted: the private key would otherwise land in logcat.
                    Log.d(TAG, "Attempt $attempts config:\n" + redact(text))
                }

                val failure = bringUp(text)
                if (failure == null) {
                    if (awaitHandshake()) {
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
                    lastFailure = Failure.NoHandshake(
                        "$endpoint accepted no handshake within " +
                            "${HANDSHAKE_TIMEOUT_MS / 1000}s using ${profile.label}"
                    )
                    Log.w(TAG, "No handshake: ${lastFailure.message}")
                } else {
                    lastFailure = failure
                    Log.w(TAG, "Attempt $attempts failed: ${failure.message}")

                    // A missing native library or denied consent will fail every
                    // remaining attempt identically. Stop and say so.
                    if (failure is Failure.NativeMissing || failure is Failure.NoPermission) {
                        runCatching { backend.setState(tunnel, Tunnel.State.DOWN, null) }
                        fail(advice(failure))
                        return
                    }
                }

                runCatching { backend.setState(tunnel, Tunnel.State.DOWN, null) }
            }
        }

        fail(advice(lastFailure ?: Failure.Other("Unknown failure")), attempts)
    }

    /** Turns a failure into something the user can act on. */
    private fun advice(failure: Failure): String = when (failure) {
        is Failure.NativeMissing ->
            "The AmneziaWG native library is missing from this build. The :tunnel " +
                "module was not compiled in - check the submodule and the NDK setup."

        is Failure.NoPermission ->
            "Android denied VPN permission. Grant the connection request, and check " +
                "whether another VPN app currently holds the always-on VPN slot."

        is Failure.BadConfig ->
            "The generated configuration was rejected: ${failure.message}"

        is Failure.NoHandshake ->
            "No server answered. The interface came up but no handshake completed, " +
                "which means the endpoints are unreachable from this network, the " +
                "port is filtered, or the keys are no longer valid. Last: " +
                failure.message

        is Failure.Other -> failure.message
    }

    private fun fail(message: String, attempts: Int = 0) {
        val suffix = if (attempts > 0) " (tried $attempts combinations)" else ""
        Log.e(TAG, "Connect failed: $message")
        stateFlow.value = stateFlow.value.copy(
            status = ConnectionStatus.FAILED,
            detail = null,
            error = message + suffix,
        )
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
            listOf(requested) + allLocations.filter { it.id != requested.id }.take(4)
        } else {
            listOf(requested)
        }

        val endpoints = pool.flatMap { loc -> loc.candidateEndpoints().map { loc to it } }
        if (endpoints.isEmpty()) return emptyList()

        val ranked = EndpointProber.rank(endpoints.map { it.second })
        val rttByEndpoint = ranked.associate { it.endpoint to it.rttMs }

        val ordered = endpoints
            .map { (loc, ep) -> Triple(loc, ep, rttByEndpoint[ep] ?: EndpointProber.UNREACHABLE) }
            .sortedWith(
                compareBy(
                    { if (it.first.id == requested.id) 0 else 1 },
                    { it.third },
                )
            )

        // A probe failure is a hint, not a verdict: UDP probes are unreliable and
        // an endpoint that looks unreachable often still handshakes. So keep the
        // unreachable ones as a fallback rather than dropping them, but only if
        // nothing better is available.
        val reachable = ordered.filter { it.third != EndpointProber.UNREACHABLE }
        return (if (reachable.isNotEmpty()) reachable + ordered.filter { it !in reachable } else ordered)
            .take(MAX_ENDPOINTS)
    }

    /** Returns null on success, or the classified reason it failed. */
    private suspend fun bringUp(configText: String): Failure? = withContext(Dispatchers.IO) {
        val config = try {
            Config.parse(ByteArrayInputStream(configText.toByteArray()))
        } catch (e: Throwable) {
            return@withContext Failure.BadConfig(e.message ?: e.javaClass.simpleName)
        }

        try {
            backend.setState(tunnel, Tunnel.State.UP, config)
            null
        } catch (e: UnsatisfiedLinkError) {
            Failure.NativeMissing(e.message ?: "libwg-go.so not found")
        } catch (e: Throwable) {
            val text = (e.message ?: e.javaClass.simpleName)
            when {
                // wireguard-android throws a plain Exception for this case, so the
                // message is the only thing to match on.
                text.contains("permission", ignoreCase = true) ||
                    text.contains("prepare", ignoreCase = true) ||
                    text.contains("consent", ignoreCase = true) -> Failure.NoPermission(text)

                text.contains("UnsatisfiedLink", ignoreCase = true) ||
                    text.contains("libwg", ignoreCase = true) -> Failure.NativeMissing(text)

                else -> Failure.Other(text)
            }
        }
    }

    /**
     * Polls backend statistics for a completed handshake. A tunnel that never
     * handshakes reports no received bytes, which is precisely the silent
     * failure the original implementation could not detect.
     *
     * Only rx counts. Transmitted bytes prove nothing - we send handshake
     * initiations into the void whether or not anything is listening.
     */
    private suspend fun awaitHandshake(): Boolean {
        val deadline = System.currentTimeMillis() + HANDSHAKE_TIMEOUT_MS
        while (System.currentTimeMillis() < deadline) {
            val stats = runCatching { backend.getStatistics(tunnel) }.getOrNull()
            if (stats != null && stats.totalRx() > 0) return true
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

    /** Strips key material so a config can be logged safely. */
    private fun redact(config: String): String =
        config.lineSequence().joinToString("\n") { line ->
            if (line.trimStart().startsWith("PrivateKey", ignoreCase = true)) {
                "PrivateKey = <redacted>"
            } else {
                line
            }
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
