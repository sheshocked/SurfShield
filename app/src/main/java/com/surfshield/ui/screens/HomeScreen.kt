package com.surfshield.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.surfshield.R
import com.surfshield.ui.components.ConnectButton
import com.surfshield.ui.theme.SurfPalette
import com.surfshield.vpn.ConnectionStatus
import com.surfshield.vpn.TunnelState

@Composable
fun HomeScreen(
    state: TunnelState,
    hapticsEnabled: Boolean,
    onToggle: () -> Unit,
    onPickServer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        SurfPalette.Surface,
                        MaterialTheme.colorScheme.background,
                    )
                )
            )
    ) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header: logo replaces the placeholder emoji the old build shipped.
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(34.dp),
                )
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "SurfShield",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "AmneziaWG \u00b7 anti-censorship",
                        style = MaterialTheme.typography.bodySmall,
                        color = SurfPalette.Muted,
                    )
                }
                Text(
                    "\u2699",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SurfPalette.Muted,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onOpenSettings)
                        .padding(10.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            ConnectButton(
                status = state.status,
                subLabel = state.detail ?: state.profile?.label,
                hapticsEnabled = hapticsEnabled,
                onClick = onToggle,
            )

            Spacer(Modifier.height(18.dp))

            AnimatedVisibility(state.error != null) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Danger,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfPalette.Danger.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            // Selected server card.
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onPickServer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    state.location?.emojiFlag ?: "\uD83C\uDF10",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        state.location?.displayName ?: "Choose a server",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        // The IP is shown deliberately: the whole point of the
                        // rework is that no hostname is ever resolved.
                        state.endpoint ?: "Smart Connect will pick the fastest",
                        style = MaterialTheme.typography.bodySmall,
                        color = SurfPalette.Muted,
                    )
                }
                Text("\u203A", color = SurfPalette.Muted)
            }

            Spacer(Modifier.height(12.dp))

            // Live throughput.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile("Download", formatRate(state.rxRateBps), Modifier.weight(1f))
                StatTile("Upload", formatRate(state.txRateBps), Modifier.weight(1f))
                StatTile(
                    "Latency",
                    state.rttMs?.let { "$it ms" } ?: "--",
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = SurfPalette.Teal)
        Text(label, style = MaterialTheme.typography.bodySmall, color = SurfPalette.Muted)
    }
}

private fun formatRate(bytesPerSecond: Long): String {
    val bits = bytesPerSecond * 8.0
    return when {
        bits >= 1_000_000_000 -> String.format("%.1f Gb/s", bits / 1_000_000_000)
        bits >= 1_000_000 -> String.format("%.1f Mb/s", bits / 1_000_000)
        bits >= 1_000 -> String.format("%.0f Kb/s", bits / 1_000)
        else -> "0"
    }
}

/** Kept so callers can render a compact status word next to the header. */
fun ConnectionStatus.shortLabel(): String = when (this) {
    ConnectionStatus.DISCONNECTED -> "Off"
    ConnectionStatus.CONNECTING -> "Connecting"
    ConnectionStatus.TESTING -> "Testing"
    ConnectionStatus.CONNECTED -> "On"
    ConnectionStatus.RECONNECTING -> "Reconnecting"
    ConnectionStatus.FAILED -> "Failed"
}
