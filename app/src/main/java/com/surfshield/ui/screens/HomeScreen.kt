package com.surfshield.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.surfshield.R
import com.surfshield.data.BackgroundStyle
import com.surfshield.ui.components.ConnectButton
import com.surfshield.ui.components.StaggeredEntrance
import com.surfshield.ui.components.SurfBackground
import com.surfshield.ui.components.motionDuration
import com.surfshield.ui.components.pressScale
import com.surfshield.ui.theme.SurfPalette
import com.surfshield.vpn.ConnectionStatus
import com.surfshield.vpn.TunnelState

@Composable
fun HomeScreen(
    state: TunnelState,
    hapticsEnabled: Boolean,
    backgroundStyle: BackgroundStyle,
    showEndpoint: Boolean,
    smartConnect: Boolean,
    onToggleSmartConnect: (Boolean) -> Unit,
    onToggle: () -> Unit,
    onPickServer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    SurfBackground(backgroundStyle) {
        Column(
            Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Header(state.status, onOpenSettings)

            Spacer(Modifier.weight(1f))

            ConnectButton(
                status = state.status,
                subLabel = state.detail ?: state.profile?.label,
                hapticsEnabled = hapticsEnabled,
                onClick = onToggle,
            )

            Spacer(Modifier.height(18.dp))

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(tween(motionDuration(240))) +
                    slideInVertically(tween(motionDuration(240))) { it / 3 },
                exit = fadeOut(tween(motionDuration(160))),
            ) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Danger,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfPalette.Danger.copy(alpha = 0.12f))
                        .border(1.dp, SurfPalette.Danger.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            StaggeredEntrance(index = 0, modifier = Modifier.fillMaxWidth()) {
                QuickToggles(smartConnect, onToggleSmartConnect, onOpenSettings)
            }

            Spacer(Modifier.height(12.dp))

            StaggeredEntrance(index = 1, modifier = Modifier.fillMaxWidth()) {
                ServerCard(state, showEndpoint, onPickServer)
            }

            Spacer(Modifier.height(12.dp))

            StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
private fun Header(status: ConnectionStatus, onOpenSettings: () -> Unit) {
    val gear = remember { MutableInteractionSource() }

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
                color = SurfPalette.OnBackground,
            )
            Text(
                "AmneziaWG \u00b7 anti-censorship",
                style = MaterialTheme.typography.bodySmall,
                color = SurfPalette.Muted,
            )
        }
        StatusPill(status)
        Spacer(Modifier.size(6.dp))
        Text(
            "\u2699",
            style = MaterialTheme.typography.headlineSmall,
            color = SurfPalette.Muted,
            modifier = Modifier
                .pressScale(gear)
                .clip(CircleShape)
                .clickable(interactionSource = gear, indication = null, onClick = onOpenSettings)
                .padding(10.dp),
        )
    }
}

/** Compact state badge, so the status is legible without reading the dial. */
@Composable
private fun StatusPill(status: ConnectionStatus) {
    val color by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.CONNECTED -> SurfPalette.Success
            ConnectionStatus.FAILED -> SurfPalette.Danger
            ConnectionStatus.DISCONNECTED -> SurfPalette.Muted
            else -> SurfPalette.Warning
        },
        animationSpec = tween(400),
        label = "pill",
    )

    Row(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.size(6.dp))
        Text(
            status.shortLabel(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
        )
    }
}

/** The two switches worth reaching without opening settings. */
@Composable
private fun QuickToggles(
    smartConnect: Boolean,
    onToggleSmartConnect: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Chip(
            label = "Smart Connect",
            active = smartConnect,
            modifier = Modifier.weight(1f),
            onClick = { onToggleSmartConnect(!smartConnect) },
        )
        Chip(
            label = "All settings",
            active = false,
            modifier = Modifier.weight(1f),
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun Chip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val background by animateColorAsState(
        targetValue = if (active) SurfPalette.Teal.copy(alpha = 0.16f) else SurfPalette.SurfaceElevated,
        animationSpec = tween(300),
        label = "chipBg",
    )
    val content by animateColorAsState(
        targetValue = if (active) SurfPalette.Teal else SurfPalette.Muted,
        animationSpec = tween(300),
        label = "chipFg",
    )

    Box(
        modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(
                1.dp,
                if (active) SurfPalette.Teal.copy(alpha = 0.4f) else SurfPalette.Outline,
                RoundedCornerShape(14.dp),
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ServerCard(state: TunnelState, showEndpoint: Boolean, onPickServer: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }

    Row(
        Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfPalette.SurfaceElevated)
            .border(1.dp, SurfPalette.Outline, RoundedCornerShape(18.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onPickServer)
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
                color = SurfPalette.OnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showEndpoint) {
                Text(
                    // The IP is shown deliberately: the whole point of the
                    // rework is that no hostname is ever resolved.
                    state.endpoint ?: "Smart Connect will pick the fastest",
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text("\u203A", style = MaterialTheme.typography.titleLarge, color = SurfPalette.Muted)
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfPalette.SurfaceElevated)
            .border(1.dp, SurfPalette.Outline, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Values slide rather than jump, which reads as a live meter.
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                val duration = motionDuration(220).coerceAtLeast(1)
                (slideInVertically(tween(duration)) { it / 2 } + fadeIn(tween(duration))) togetherWith
                    (slideOutVertically(tween(duration)) { -it / 2 } + fadeOut(tween(duration)))
            },
            label = "stat",
        ) { shown ->
            Text(
                shown,
                style = MaterialTheme.typography.titleMedium,
                color = SurfPalette.Teal,
                maxLines = 1,
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = SurfPalette.Muted,
            maxLines = 1,
        )
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
