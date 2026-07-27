package com.surfshield

import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.surfshield.data.AppSettings
import com.surfshield.data.LocationRepository
import com.surfshield.data.SurfLocation
import com.surfshield.net.EndpointProber
import com.surfshield.ui.screens.HomeScreen
import com.surfshield.ui.screens.ServerListScreen
import com.surfshield.ui.screens.SettingsScreen
import com.surfshield.ui.screens.SplitTunnelScreen
import com.surfshield.ui.theme.SurfShieldTheme
import com.surfshield.vpn.TunnelManager
import kotlinx.coroutines.launch

private enum class Screen { HOME, SERVERS, SETTINGS, SPLIT_TUNNEL }

/** One level up from [this], or null when already at the root. */
private fun Screen.parent(): Screen? = when (this) {
    Screen.HOME -> null
    Screen.SERVERS, Screen.SETTINGS -> Screen.HOME
    Screen.SPLIT_TUNNEL -> Screen.SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var settings: AppSettings
    private lateinit var tunnel: TunnelManager

    /** Pending connect target, held while the system VPN consent dialog is up. */
    private var pendingConnect: SurfLocation? = null
    private var pendingPool: List<SurfLocation> = emptyList()

    private val vpnPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val target = pendingConnect
        pendingConnect = null
        if (result.resultCode == RESULT_OK && target != null) {
            tunnel.connect(target, pendingPool)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settings = AppSettings.get(this)
        tunnel = TunnelManager.get(this)

        setContent { App() }
    }

    /**
     * Android requires explicit user consent before any app may create a VPN
     * interface. prepare() returns an Intent the first time and null once
     * consent has been granted, so this must be checked on every connect.
     */
    private fun requestConnect(location: SurfLocation, pool: List<SurfLocation>) {
        val consent = VpnService.prepare(this)
        if (consent == null) {
            tunnel.connect(location, pool)
        } else {
            pendingConnect = location
            pendingPool = pool
            vpnPermission.launch(consent)
        }
    }

    @Composable
    private fun App() {
        val revision by settings.revision.collectAsState()
        val state by tunnel.state.collectAsState()
        val context = LocalContext.current

        var screen by remember { mutableStateOf(Screen.HOME) }
        var locations by remember { mutableStateOf<List<SurfLocation>>(emptyList()) }
        var pings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
        var selectedId by remember { mutableStateOf(settings.lastLocationId) }

        // Back moves up the hierarchy instead of leaving the app. Without this
        // the system default applied and back from any screen closed SurfShield.
        BackHandler(enabled = screen.parent() != null) {
            screen = screen.parent() ?: Screen.HOME
        }

        // Load the profiles once, then optionally measure them.
        LaunchedEffect(Unit) {
            val loaded = LocationRepository.load(context)
            locations = loaded
            if (selectedId == null) selectedId = loaded.firstOrNull()?.id
            if (settings.probeOnLaunch && loaded.isNotEmpty()) {
                pings = EndpointProber.rank(loaded.map { it.endpoint })
                    .associate { it.endpoint to it.rttMs }
            }
        }

        val selected = locations.firstOrNull { it.id == selectedId } ?: locations.firstOrNull()

        SurfShieldTheme(
            themeMode = settings.themeMode,
            motionEnabled = settings.animationsEnabled,
        ) {
            when (screen) {
                Screen.HOME -> HomeScreen(
                    state = state.copy(location = state.location ?: selected),
                    hapticsEnabled = settings.hapticFeedback,
                    onToggle = {
                        if (state.isActive) {
                            tunnel.disconnect()
                        } else {
                            selected?.let { requestConnect(it, locations) }
                        }
                    },
                    onPickServer = { screen = Screen.SERVERS },
                    onOpenSettings = { screen = Screen.SETTINGS },
                )

                Screen.SERVERS -> ServerListScreen(
                    locations = locations,
                    pings = pings,
                    selectedId = selected?.id,
                    smartConnectEnabled = settings.smartConnect,
                    onSelect = { location ->
                        selectedId = location.id
                        settings.lastLocationId = location.id
                        screen = Screen.HOME
                        requestConnect(location, locations)
                    },
                    onSmartConnect = {
                        // Hand the whole pool to the manager and let it rank and
                        // verify, rather than trusting a single latency reading.
                        val best = pings.entries
                            .filter { it.value != EndpointProber.UNREACHABLE }
                            .minByOrNull { it.value }
                            ?.key
                        val target = locations.firstOrNull { it.endpoint == best }
                            ?: selected
                            ?: return@ServerListScreen
                        selectedId = target.id
                        screen = Screen.HOME
                        requestConnect(target, locations)
                    },
                    onRefreshPings = {
                        lifecycleScope.launch {
                            pings = EndpointProber.rank(locations.map { it.endpoint })
                                .associate { it.endpoint to it.rttMs }
                        }
                    },
                    onBack = { screen = Screen.HOME },
                )

                Screen.SETTINGS -> SettingsScreen(
                    settings = settings,
                    revision = revision,
                    onOpenSplitTunnel = { screen = Screen.SPLIT_TUNNEL },
                    onResetLearnedProfiles = { tunnel.resetLearnedProfiles() },
                    onBack = { screen = Screen.HOME },
                )

                Screen.SPLIT_TUNNEL -> SplitTunnelScreen(
                    settings = settings,
                    revision = revision,
                    onBack = { screen = Screen.SETTINGS },
                )
            }
        }
    }
}
