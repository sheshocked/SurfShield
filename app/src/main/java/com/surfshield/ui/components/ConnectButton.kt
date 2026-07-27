package com.surfshield.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surfshield.ui.theme.ConnectGradient
import com.surfshield.ui.theme.LocalMotionEnabled
import com.surfshield.ui.theme.SurfPalette
import com.surfshield.vpn.ConnectionStatus

/**
 * The single focal point of the home screen.
 *
 * Layers, outermost first: a breathing halo that only pulses while active, two
 * counter-rotating gradient arcs shown during connect/test so progress reads as
 * activity rather than a spinner, a soft filled core, and the label. Every
 * animation is gated on the reduce-motion preference.
 */
@Composable
fun ConnectButton(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    label: String? = null,
    subLabel: String? = null,
    hapticsEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    val motion = LocalMotionEnabled.current
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val busy = status == ConnectionStatus.CONNECTING ||
        status == ConnectionStatus.TESTING ||
        status == ConnectionStatus.RECONNECTING

    val accent by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.CONNECTED -> SurfPalette.Success
            ConnectionStatus.FAILED -> SurfPalette.Danger
            ConnectionStatus.DISCONNECTED -> SurfPalette.Muted
            else -> SurfPalette.Cyan
        },
        animationSpec = tween(500),
        label = "accent",
    )

    val transition = rememberInfiniteTransition(label = "connect")

    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_400, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse",
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_600, easing = LinearEasing), RepeatMode.Restart),
        label = "spin",
    )
    val breathe by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2_800, easing = LinearEasing), RepeatMode.Reverse),
        label = "breathe",
    )

    val scale = when {
        pressed -> 0.94f
        !motion -> 1f
        status == ConnectionStatus.CONNECTED -> breathe
        else -> 1f
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .selectable(
                selected = status == ConnectionStatus.CONNECTED,
                enabled = true,
                role = null,
                interactionSource = interaction,
                indication = null,
                onClick = {
                    if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val outer = this.size.minDimension / 2f

            // Expanding halo: reads as "protected and alive" without stealing focus.
            if (motion && (status == ConnectionStatus.CONNECTED || busy)) {
                listOf(0f, 0.5f).forEach { offset ->
                    val p = (pulse + offset) % 1f
                    drawCircle(
                        color = accent.copy(alpha = (1f - p) * 0.18f),
                        radius = outer * (0.62f + p * 0.38f),
                        center = center,
                    )
                }
            }

            // Static track.
            drawCircle(
                color = SurfPalette.Outline,
                radius = outer * 0.78f,
                center = center,
                style = Stroke(width = 6f),
            )

            // Two counter-rotating arcs while a connection attempt is in flight.
            if (busy) {
                val sweepBrush = Brush.sweepGradient(ConnectGradient + ConnectGradient.first())
                val radius = outer * 0.78f
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2, radius * 2)
                val angle = if (motion) spin else 0f

                drawArc(
                    brush = sweepBrush,
                    startAngle = angle,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 10f, cap = StrokeCap.Round),
                )
                drawArc(
                    brush = sweepBrush,
                    startAngle = -angle * 0.7f + 180f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 5f, cap = StrokeCap.Round),
                )
            }

            // Connected ring, drawn solid so the state is unmistakable at a glance.
            if (status == ConnectionStatus.CONNECTED) {
                drawCircle(
                    color = accent,
                    radius = outer * 0.78f,
                    center = center,
                    style = Stroke(width = 8f),
                )
            }

            // Core.
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                    center = center,
                    radius = outer * 0.72f,
                ),
                radius = outer * 0.72f,
                center = center,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label ?: defaultLabel(status),
                style = MaterialTheme.typography.headlineSmall,
                color = accent,
                textAlign = TextAlign.Center,
            )
            if (!subLabel.isNullOrBlank()) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = SurfPalette.Muted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun defaultLabel(status: ConnectionStatus) = when (status) {
    ConnectionStatus.DISCONNECTED -> "Tap to connect"
    ConnectionStatus.CONNECTING -> "Connecting"
    ConnectionStatus.TESTING -> "Finding best route"
    ConnectionStatus.CONNECTED -> "Protected"
    ConnectionStatus.RECONNECTING -> "Reconnecting"
    ConnectionStatus.FAILED -> "Failed - retry"
}
