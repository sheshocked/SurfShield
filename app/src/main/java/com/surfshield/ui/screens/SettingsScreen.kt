package com.surfshield.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surfshield.data.AmneziaParams
import com.surfshield.data.AppSettings
import com.surfshield.data.DnsPreset
import com.surfshield.data.ObfuscationMode
import com.surfshield.data.SplitTunnelMode
import com.surfshield.data.ThemeMode
import com.surfshield.ui.components.ActionRow
import com.surfshield.ui.components.NavigationRow
import com.surfshield.ui.components.RadioRow
import com.surfshield.ui.components.SegmentedRow
import com.surfshield.ui.components.SettingsGroup
import com.surfshield.ui.components.SliderRow
import com.surfshield.ui.components.SwitchRow
import com.surfshield.ui.components.TextFieldRow
import com.surfshield.ui.theme.SurfPalette

/** Default MTU offered when the user turns automatic off. */
private const val MTU_FALLBACK = 1420
private const val KEEPALIVE_FALLBACK = 25

@Composable
fun SettingsScreen(
    settings: AppSettings,
    revision: Long,
    onOpenSplitTunnel: () -> Unit,
    onResetLearnedProfiles: () -> Unit,
    onBack: () -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }
    var confirmForget by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(SurfPalette.Navy)
    ) {
        TopBar(title = "Settings", onBack = onBack)

        // Keyed on revision so a write from any row recomposes the whole screen.
        key(revision) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                ConnectionGroup(settings)
                ObfuscationGroup(settings) { confirmForget = true }
                CustomAwgGroup(settings)
                DnsGroup(settings)
                RoutingGroup(settings, onOpenSplitTunnel)
                TuningGroup(settings)
                AppearanceGroup(settings)
                AdvancedGroup(settings) { confirmReset = true }
            }
        }
    }

    if (confirmForget) {
        ConfirmDialog(
            title = "Forget learned profiles?",
            body = "The app remembers which obfuscation level worked on each network. " +
                "Clearing that means the next connection has to search again, which is " +
                "slower but useful after changing ISP.",
            confirmLabel = "Forget",
            onConfirm = {
                onResetLearnedProfiles()
                confirmForget = false
            },
            onDismiss = { confirmForget = false },
        )
    }

    if (confirmReset) {
        ConfirmDialog(
            title = "Reset all settings?",
            body = "Every preference returns to its default, including your split " +
                "tunnelling selection. Server profiles are not affected.",
            confirmLabel = "Reset",
            danger = true,
            onConfirm = {
                settings.resetToDefaults()
                confirmReset = false
            },
            onDismiss = { confirmReset = false },
        )
    }
}

// ----------------------------------------------------------------------- groups

@Composable
private fun ConnectionGroup(settings: AppSettings) {
    SettingsGroup("Connection") {
        SwitchRow(
            title = "Smart Connect",
            subtitle = "Measure every endpoint and connect to the fastest that answers, " +
                "instead of trusting the one you picked",
            checked = settings.smartConnect,
        ) { settings.smartConnect = it }

        SwitchRow(
            title = "Measure servers on launch",
            subtitle = "Fills in the latency badges. Costs a little data",
            checked = settings.probeOnLaunch,
        ) { settings.probeOnLaunch = it }

        SwitchRow(
            title = "Reconnect on network change",
            subtitle = "Rebuild the tunnel when moving between Wi-Fi and mobile data",
            checked = settings.reconnectOnNetworkChange,
        ) { settings.reconnectOnNetworkChange = it }

        SwitchRow(
            title = "Connect on boot",
            subtitle = "Reconnect to the last server after the device restarts",
            checked = settings.autoConnectOnBoot,
        ) { settings.autoConnectOnBoot = it }

        SwitchRow(
            title = "Connect on untrusted Wi-Fi",
            subtitle = "Connect automatically on any Wi-Fi network you have not marked trusted",
            checked = settings.autoConnectOnUntrustedWifi,
        ) { settings.autoConnectOnUntrustedWifi = it }

        SwitchRow(
            title = "Kill switch",
            subtitle = "Block all traffic while the tunnel is down rather than letting it " +
                "leave unencrypted",
            checked = settings.killSwitch,
        ) { settings.killSwitch = it }
    }
}

@Composable
private fun ObfuscationGroup(settings: AppSettings, onForget: () -> Unit) {
    SettingsGroup(
        title = "Obfuscation",
        footnote = "Automatic starts with the lightest setting that has worked on this " +
            "network before and escalates only if the handshake fails. Higher is not " +
            "better - it costs bandwidth and, past a point, stops working entirely " +
            "against servers that do not run AmneziaWG.",
    ) {
        SegmentedRow(
            title = "Level",
            options = ObfuscationMode.entries.toList(),
            selected = settings.obfuscationMode,
            label = { mode ->
                when (mode) {
                    ObfuscationMode.AUTO -> "Automatic"
                    ObfuscationMode.PLAIN -> "Off"
                    ObfuscationMode.LIGHT -> "Light"
                    ObfuscationMode.MEDIUM -> "Medium"
                    ObfuscationMode.HEAVY -> "Heavy"
                    ObfuscationMode.FULL_AWG -> "Full AWG"
                    ObfuscationMode.CUSTOM -> "Custom"
                }
            },
            subtitle = describeObfuscation(settings.obfuscationMode),
        ) { settings.obfuscationMode = it }

        ActionRow(
            title = "Forget learned profiles",
            subtitle = "Start the search from scratch on the next connection",
            onClick = onForget,
        )
    }
}

private fun describeObfuscation(mode: ObfuscationMode): String = when (mode) {
    ObfuscationMode.AUTO -> "Escalates only as far as the network requires"
    ObfuscationMode.PLAIN -> "Plain WireGuard. Fastest, and the easiest to fingerprint"
    ObfuscationMode.LIGHT -> "A few junk packets before the handshake. Works with any server"
    ObfuscationMode.MEDIUM -> "More junk, smaller MTU. A good default under active filtering"
    ObfuscationMode.HEAVY -> "Heavy junk padding. Noticeable overhead, still server-agnostic"
    ObfuscationMode.FULL_AWG -> "Rewrites the handshake itself. Only works against an " +
        "AmneziaWG server - Surfshark endpoints will never connect on this setting"
    ObfuscationMode.CUSTOM -> "Your own parameters, below"
}

@Composable
private fun CustomAwgGroup(settings: AppSettings) {
    if (settings.obfuscationMode != ObfuscationMode.CUSTOM) return
    val p = settings.customAmnezia

    SettingsGroup(
        title = "Custom AmneziaWG parameters",
        footnote = "Jc, Jmin and Jmax send junk packets before the handshake. A stock " +
            "WireGuard server ignores them, so they are safe everywhere and are what " +
            "actually defeats fingerprinting of the fixed-size handshake. S1, S2 and " +
            "H1-H4 rewrite the handshake packets and REQUIRE AmneziaWG at both ends: " +
            "set them against Surfshark and the interface will come up but never pass " +
            "traffic. Only your own server supports them.",
    ) {
        SliderRow(
            title = "Jc - junk packet count",
            value = p.jc,
            range = 0..12,
            subtitle = "Packets sent before the real handshake",
        ) { settings.customAmnezia = p.copy(jc = it) }

        SliderRow(
            title = "Jmin - minimum junk size",
            value = p.jmin,
            range = 8..600,
            valueLabel = { "$it B" },
        ) { settings.customAmnezia = p.copy(jmin = it.coerceAtMost(p.jmax)) }

        SliderRow(
            title = "Jmax - maximum junk size",
            value = p.jmax,
            range = 16..1280,
            valueLabel = { "$it B" },
        ) { settings.customAmnezia = p.copy(jmax = it.coerceAtLeast(p.jmin)) }

        SliderRow(
            title = "S1 - init packet junk",
            value = p.s1,
            range = 0..128,
            subtitle = "Requires AmneziaWG on the server",
        ) { settings.customAmnezia = p.copy(s1 = it) }

        SliderRow(
            title = "S2 - response packet junk",
            value = p.s2,
            range = 0..128,
            subtitle = "Requires AmneziaWG on the server",
        ) { settings.customAmnezia = p.copy(s2 = it) }

        ActionRow(
            title = "Reset to vanilla-safe values",
            subtitle = "Keeps junk packets, clears everything that needs an AmneziaWG server",
            onClick = { settings.customAmnezia = AmneziaParams.LIGHT },
        )
    }
}

@Composable
private fun DnsGroup(settings: AppSettings) {
    val preset = settings.dnsPreset
    val customText = settings.customDns.joinToString(", ")
    val invalid = preset == DnsPreset.CUSTOM &&
        customText.isNotBlank() &&
        !customText.split(',').all { isIpv4(it.trim()) }

    SettingsGroup(
        title = "DNS",
        footnote = "DNS queries travel inside the tunnel, so the resolver sees traffic " +
            "from the server's location rather than yours. Shecan resolves domestic " +
            "sites that refuse foreign addresses, but it is inside Iran - do not pair " +
            "it with a tunnel you are using for privacy.",
    ) {
        DnsPreset.entries.forEach { option ->
            RadioRow(
                title = option.label,
                subtitle = when {
                    option == DnsPreset.SERVER -> "Whatever the selected profile specifies"
                    option == DnsPreset.CUSTOM -> "Enter your own resolvers"
                    else -> option.servers.joinToString(", ")
                },
                selected = preset == option,
            ) { settings.dnsPreset = option }
        }

        TextFieldRow(
            title = "Custom resolvers",
            value = customText,
            placeholder = "1.1.1.1, 9.9.9.9",
            subtitle = "Comma separated IPv4 addresses",
            error = if (invalid) "Not a valid IPv4 address list" else null,
            visible = preset == DnsPreset.CUSTOM,
        ) { text ->
            settings.customDns = text.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}

private fun isIpv4(value: String): Boolean {
    val parts = value.split('.')
    if (parts.size != 4) return false
    return parts.all { part ->
        part.isNotEmpty() && part.all(Char::isDigit) && (part.toIntOrNull() ?: 256) in 0..255
    }
}

@Composable
private fun RoutingGroup(settings: AppSettings, onOpenSplitTunnel: () -> Unit) {
    val appCount = settings.splitTunnelApps.size

    SettingsGroup("Routing") {
        NavigationRow(
            title = "Split tunnelling",
            value = when (settings.splitTunnelMode) {
                SplitTunnelMode.OFF -> "Off"
                SplitTunnelMode.EXCLUDE -> "$appCount excluded"
                SplitTunnelMode.INCLUDE -> "$appCount only"
            },
            subtitle = "Choose which apps bypass the tunnel, or which are the only ones to use it",
            onClick = onOpenSplitTunnel,
        )

        SwitchRow(
            title = "Bypass local network",
            subtitle = "Keep printers, NAS and the router page reachable while connected",
            checked = settings.bypassLan,
        ) { settings.bypassLan = it }

        SwitchRow(
            title = "Route Iranian addresses directly",
            subtitle = "Domestic sites stay on the local path - faster, and some of them " +
                "reject foreign addresses outright",
            checked = settings.bypassIranianRanges,
        ) { settings.bypassIranianRanges = it }

        SwitchRow(
            title = "IPv6 inside the tunnel",
            subtitle = "Leave off unless the server offers IPv6. Enabling it against a " +
                "v4-only peer blackholes all IPv6 traffic",
            checked = settings.ipv6Enabled,
        ) { settings.ipv6Enabled = it }
    }
}

@Composable
private fun TuningGroup(settings: AppSettings) {
    val autoMtu = settings.mtuOverride == 0
    val autoKeepalive = settings.keepaliveOverride == 0

    SettingsGroup(
        title = "Tunnel tuning",
        footnote = "Lower the MTU if pages hang halfway through loading while the " +
            "connection still reports as up - that is the classic sign of fragmented " +
            "packets being dropped. 1280 is the safe floor.",
    ) {
        SwitchRow(
            title = "Automatic MTU",
            subtitle = "Let the active obfuscation level choose",
            checked = autoMtu,
        ) { settings.mtuOverride = if (it) 0 else MTU_FALLBACK }

        SliderRow(
            title = "MTU",
            value = if (autoMtu) MTU_FALLBACK else settings.mtuOverride,
            range = 1280..1500,
            steps = 21,
            enabled = !autoMtu,
            subtitle = if (autoMtu) "Turn off automatic to adjust" else null,
        ) { settings.mtuOverride = it }

        SwitchRow(
            title = "Automatic keepalive",
            subtitle = "Let the active obfuscation level choose",
            checked = autoKeepalive,
        ) { settings.keepaliveOverride = if (it) 0 else KEEPALIVE_FALLBACK }

        SliderRow(
            title = "Persistent keepalive",
            value = if (autoKeepalive) KEEPALIVE_FALLBACK else settings.keepaliveOverride,
            range = 5..120,
            enabled = !autoKeepalive,
            valueLabel = { "$it s" },
            subtitle = if (autoKeepalive) {
                "Turn off automatic to adjust"
            } else {
                "Keeps the NAT mapping open. Lower survives aggressive routers, costs battery"
            },
        ) { settings.keepaliveOverride = it }
    }
}

@Composable
private fun AppearanceGroup(settings: AppSettings) {
    SettingsGroup("Appearance") {
        SegmentedRow(
            title = "Theme",
            options = ThemeMode.entries.toList(),
            selected = settings.themeMode,
            label = {
                when (it) {
                    ThemeMode.SYSTEM -> "System"
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.AMOLED -> "AMOLED"
                }
            },
        ) { settings.themeMode = it }

        SwitchRow(
            title = "Animations",
            subtitle = "Disable for a snappier feel on older devices",
            checked = settings.animationsEnabled,
        ) { settings.animationsEnabled = it }

        SwitchRow(
            title = "Haptic feedback",
            subtitle = "Vibrate when connecting and disconnecting",
            checked = settings.hapticFeedback,
        ) { settings.hapticFeedback = it }
    }
}

@Composable
private fun AdvancedGroup(settings: AppSettings, onReset: () -> Unit) {
    SettingsGroup("Advanced") {
        SwitchRow(
            title = "Show speed in the notification",
            subtitle = "Live throughput on the ongoing VPN notification",
            checked = settings.showStatsInNotification,
        ) { settings.showStatsInNotification = it }

        SwitchRow(
            title = "Verbose logging",
            subtitle = "Log each connection attempt to logcat. Private keys are redacted",
            checked = settings.verboseLogging,
        ) { settings.verboseLogging = it }

        ActionRow(
            title = "Reset all settings",
            subtitle = "Return every preference to its default",
            danger = true,
            onClick = onReset,
        )
    }
}

// ------------------------------------------------------------------- chrome

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "<",
            style = MaterialTheme.typography.titleLarge,
            color = SurfPalette.OnBackground,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 4.dp),
        )
        Spacer(Modifier.padding(horizontal = 2.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = SurfPalette.OnBackground,
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    body: String,
    confirmLabel: String,
    danger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfPalette.SurfaceElevated,
        title = { Text(title, color = SurfPalette.OnBackground) },
        text = { Text(body, color = SurfPalette.Muted) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmLabel,
                    color = if (danger) SurfPalette.Danger else SurfPalette.Teal,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SurfPalette.Muted)
            }
        },
    )
}
