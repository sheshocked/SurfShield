package com.surfshield.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.surfshield.data.AmneziaParams
import com.surfshield.data.AppSettings
import com.surfshield.data.DnsPreset
import com.surfshield.data.ObfuscationMode
import com.surfshield.data.SplitTunnelMode
import com.surfshield.data.ThemeMode
import com.surfshield.ui.components.NavigationRow
import com.surfshield.ui.components.SegmentedRow
import com.surfshield.ui.components.SettingsGroup
import com.surfshield.ui.components.SliderRow
import com.surfshield.ui.components.SwitchRow
import com.surfshield.ui.components.TextFieldRow
import com.surfshield.ui.theme.SurfPalette

/**
 * Every setting the engine honours, grouped by intent.
 *
 * [revision] is passed in purely to force recomposition after a write, because
 * AppSettings is a plain SharedPreferences wrapper rather than a set of
 * individual flows.
 */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    @Suppress("UNUSED_PARAMETER") revision: Long,
    onOpenSplitTunnel: () -> Unit,
    onOpenTheme: () -> Unit,
    onResetLearnedProfiles: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "\u2039",
                style = MaterialTheme.typography.displaySmall,
                color = SurfPalette.Muted,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 14.dp),
            )
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
        }

        // ------------------------------------------------------------- connection
        SettingsGroup("Connection") {
            SwitchRow(
                title = "Smart Connect",
                subtitle = "Measure every endpoint and use the fastest one that answers",
                checked = settings.smartConnect,
            ) { settings.smartConnect = it }

            SwitchRow(
                title = "Probe servers on launch",
                subtitle = "Fills the latency badges in the server list",
                checked = settings.probeOnLaunch,
            ) { settings.probeOnLaunch = it }

            SwitchRow(
                title = "Reconnect on network change",
                subtitle = "Rebuild the tunnel when moving between Wi-Fi and mobile data",
                checked = settings.reconnectOnNetworkChange,
            ) { settings.reconnectOnNetworkChange = it }

            SwitchRow(
                title = "Connect on boot",
                checked = settings.autoConnectOnBoot,
            ) { settings.autoConnectOnBoot = it }

            SwitchRow(
                title = "Connect on untrusted Wi-Fi",
                subtitle = "Any network you have not marked as trusted",
                checked = settings.autoConnectOnUntrustedWifi,
            ) { settings.autoConnectOnUntrustedWifi = it }

            SwitchRow(
                title = "Kill switch",
                subtitle = "Block all traffic while the tunnel is down instead of leaking in clear",
                checked = settings.killSwitch,
            ) { settings.killSwitch = it }
        }

        // ------------------------------------------------------------ obfuscation
        SettingsGroup("Obfuscation") {
            SegmentedRow(
                title = "Mode",
                subtitle = "Auto starts light and escalates only if the handshake fails, " +
                    "then remembers what worked on this network",
                options = ObfuscationMode.entries.toList(),
                selected = settings.obfuscationMode,
                label = { it.name.lowercase().replace('_', ' ') },
            ) { settings.obfuscationMode = it }

            val custom = settings.obfuscationMode == ObfuscationMode.CUSTOM
            if (custom) {
                val p = settings.customAmnezia

                SliderRow(
                    title = "Jc - junk packet count",
                    subtitle = "Safe against any WireGuard server",
                    value = p.jc,
                    range = 0..12,
                ) { settings.customAmnezia = p.copy(jc = it) }

                SliderRow(
                    title = "Jmin - smallest junk packet",
                    value = p.jmin,
                    range = 0..1_200,
                ) { settings.customAmnezia = p.copy(jmin = it) }

                SliderRow(
                    title = "Jmax - largest junk packet",
                    value = p.jmax,
                    range = 0..1_280,
                ) { settings.customAmnezia = p.copy(jmax = it) }

                SliderRow(
                    title = "S1 - junk before handshake init",
                    subtitle = "Requires an AmneziaWG server",
                    value = p.s1,
                    range = 0..150,
                ) { settings.customAmnezia = p.copy(s1 = it) }

                SliderRow(
                    title = "S2 - junk before handshake response",
                    subtitle = "Requires an AmneziaWG server",
                    value = p.s2,
                    range = 0..150,
                ) { settings.customAmnezia = p.copy(s2 = it) }

                if (!p.isVanillaCompatible) {
                    Text(
                        text = "S1, S2 and H1-H4 rewrite the handshake itself. A stock " +
                            "WireGuard server such as Surfshark cannot decode them and the " +
                            "tunnel will never come up. SurfShield falls back to " +
                            "vanilla-safe values automatically for those servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SurfPalette.Warning,
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfPalette.Warning.copy(alpha = 0.10f))
                            .padding(12.dp),
                    )
                }

                NavigationRow(
                    title = "Reset Amnezia parameters",
                    value = "defaults",
                ) { settings.customAmnezia = AmneziaParams.LIGHT }
            }

            NavigationRow(
                title = "Forget learned settings",
                subtitle = "Clear the profile remembered for each network, e.g. after changing ISP",
                onClick = onResetLearnedProfiles,
            )
        }

        // ---------------------------------------------------------------- routing
        SettingsGroup("Routing") {
            NavigationRow(
                title = "Split tunnelling",
                value = when (settings.splitTunnelMode) {
                    SplitTunnelMode.OFF -> "Off"
                    SplitTunnelMode.EXCLUDE -> "${settings.splitTunnelApps.size} excluded"
                    SplitTunnelMode.INCLUDE -> "${settings.splitTunnelApps.size} included"
                },
                subtitle = "Choose which apps bypass the tunnel, or route only selected apps",
                onClick = onOpenSplitTunnel,
            )

            SwitchRow(
                title = "Bypass local network",
                subtitle = "Keep printers, NAS and the router page reachable while connected",
                checked = settings.bypassLan,
            ) { settings.bypassLan = it }

            SwitchRow(
                title = "Iranian sites direct",
                subtitle = "Route domestic address space outside the tunnel so local sites " +
                    "stay fast and do not see a foreign IP",
                checked = settings.bypassIranianRanges,
            ) { settings.bypassIranianRanges = it }

            SwitchRow(
                title = "IPv6 inside the tunnel",
                subtitle = "Leave off unless your server has working IPv6",
                checked = settings.ipv6Enabled,
            ) { settings.ipv6Enabled = it }
        }

        // -------------------------------------------------------------------- dns
        SettingsGroup("DNS") {
            SegmentedRow(
                title = "Resolver",
                options = DnsPreset.entries.toList(),
                selected = settings.dnsPreset,
                label = { it.name.lowercase() },
            ) { settings.dnsPreset = it }

            TextFieldRow(
                title = "Custom servers",
                subtitle = "Comma separated, e.g. 1.1.1.1, 9.9.9.9",
                value = settings.customDns.joinToString(", "),
                placeholder = "1.1.1.1, 1.0.0.1",
                visible = settings.dnsPreset == DnsPreset.CUSTOM,
            ) { settings.customDns = it.split(',').map(String::trim).filter(String::isNotEmpty) }
        }

        // ----------------------------------------------------------------- tuning
        SettingsGroup("Tunnel tuning") {
            val currentMtu = settings.mtuOverride
            SliderRow(
                title = "MTU",
                subtitle = "auto (leftmost) lets the active profile decide. Lower it if large pages stall.",
                value = if (currentMtu == 0) 1279 else currentMtu,
                range = 1279..1500,
                valueLabel = { if (it == 1279) "auto" else it.toString() },
            ) { settings.mtuOverride = if (it == 1279) 0 else it }

            SliderRow(
                title = "Persistent keepalive",
                subtitle = "Seconds. Lower values survive aggressive NAT timeouts at the " +
                    "cost of a little battery.",
                value = settings.keepaliveOverride,
                range = 0..120,
                valueLabel = { if (it == 0) "auto" else "${it}s" },
            ) { settings.keepaliveOverride = it }
        }

        // ------------------------------------------------------------- appearance
        SettingsGroup("Appearance") {
            NavigationRow(
                title = "Theme",
                value = when (settings.themeMode) {
                    ThemeMode.SYSTEM -> "system"
                    ThemeMode.DARK -> "dark"
                    ThemeMode.AMOLED -> "amoled"
                },
                subtitle = "Custom theme colors and tweaks",
                onClick = onOpenTheme
            )

            SegmentedRow(
                title = "Language",
                options = listOf("system", "fa", "en"),
                selected = settings.language ?: "system",
                label = { it },
            ) { settings.language = it.takeIf { v -> v != "system" } }

            SwitchRow(
                title = "Animations",
                subtitle = "Turn off to reduce motion and save a little battery",
                checked = settings.animationsEnabled,
            ) { settings.animationsEnabled = it }

            SwitchRow(
                title = "Haptic feedback",
                checked = settings.hapticFeedback,
            ) { settings.hapticFeedback = it }

            SwitchRow(
                title = "Show speed in notification",
                checked = settings.showStatsInNotification,
            ) { settings.showStatsInNotification = it }
        }

        // ---------------------------------------------------------------- advanced
        SettingsGroup("Advanced") {
            SwitchRow(
                title = "Verbose logging",
                subtitle = "Only for troubleshooting. Logs stay on the device.",
                checked = settings.verboseLogging,
            ) { settings.verboseLogging = it }

            NavigationRow(
                title = "Reset all settings",
                value = "defaults",
            ) { settings.resetToDefaults() }
        }

        Spacer(Modifier.height(32.dp))
    }
}
