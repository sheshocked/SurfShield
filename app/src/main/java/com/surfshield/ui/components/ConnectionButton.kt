package com.surfshield.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surfshield.ui.ConnectionStatus
import com.surfshield.ui.SurfColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ConnectionButton(
    status: ConnectionStatus,
    size: Dp = 280.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Pulse animation for connecting state
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (status == ConnectionStatus.CONNECTING) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Glow animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (status == ConnectionStatus.CONNECTED) 0.8f
                     else if (status == ConnectionStatus.CONNECTING) 0.6f
                     else 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Rotation animation for connecting
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (status == ConnectionStatus.CONNECTING) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    val outerColor = when (status) {
        ConnectionStatus.CONNECTED -> SurfColors.StatusConnected
        ConnectionStatus.CONNECTING -> SurfColors.StatusConnecting
        ConnectionStatus.ERROR -> SurfColors.StatusError
        ConnectionStatus.DISCONNECTED -> SurfColors.Muted
    }

    val innerColor = when (status) {
        ConnectionStatus.CONNECTED -> SurfColors.PrimaryGlow
        ConnectionStatus.CONNECTING -> SurfColors.Primary
        else -> SurfColors.SurfaceElevated
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        if (status != ConnectionStatus.DISCONNECTED) {
            Canvas(modifier = Modifier.size(size * (1.2f * pulseScale))) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * pulseScale * 1.1f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            outerColor.copy(alpha = glowAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }
        }

        // Animated outer ring
        Canvas(modifier = Modifier.size(size * pulseScale)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 * 0.95f

            // Dashed arc rotation
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(outerColor, outerColor.copy(alpha = 0.3f), Color.Transparent, outerColor),
                    center = center
                ),
                startAngle = rotation,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
        }

        // Main circle
        Box(
            modifier = Modifier
                .size(size * 0.7f)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(innerColor, SurfColors.Surface),
                        center = Offset(0.3f, 0.3f),
                        radius = 0.8f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner glow
            if (status == ConnectionStatus.CONNECTED) {
                Canvas(modifier = Modifier.size(size * 0.5f)) {
                    val center = Offset(size.width / 2, size.height / 2)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                outerColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }
            }

            // Icon or text
            val iconSize = size * 0.25f
            Box(modifier = Modifier.size(iconSize)) {
                when (status) {
                    ConnectionStatus.DISCONNECTED -> DisconnectedIcon()
                    ConnectionStatus.CONNECTING -> ConnectingIcon()
                    ConnectionStatus.CONNECTED -> ConnectedIcon()
                    ConnectionStatus.ERROR -> ErrorIcon()
                }
            }
        }

        // Click handler
        androidx.compose.foundation.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ).let { modifier
            .matchParentSize()
            .clip(CircleShape) }
    }
}

@Composable
private fun DisconnectedIcon() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val c = center
        val r = size.minDimension / 3
        // Power icon
        drawCircle(Color.White.copy(alpha = 0.5f), r * 0.15f, Offset(c.x, c.y + r * 0.6f))
        drawArc(
            Color.White.copy(alpha = 0.5f),
            -90f, 270f,
            false,
            style = Stroke(width = r * 0.25f, cap = StrokeCap.Round),
            topLeft = Offset(c.x - r, c.y - r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
        )
    }
}

@Composable
private fun ConnectedIcon() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val c = center
        val r = size.minDimension / 3
        // Shield checkmark
        drawArc(
            SurfColors.StatusConnected,
            -140f, 280f,
            false,
            style = Stroke(width = r * 0.25f, cap = StrokeCap.Round),
            topLeft = Offset(c.x - r * 1.1f, c.y - r * 1.1f),
            size = androidx.compose.ui.geometry.Size(r * 2.2f, r * 2.2f)
        )
        // Checkmark
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(c.x - r * 0.4f, c.y)
            lineTo(c.x - r * 0.1f, c.y + r * 0.4f)
            lineTo(c.x + r * 0.5f, c.y - r * 0.3f)
        }
        drawPath(path, SurfColors.StatusConnected, style = Stroke(r * 0.15f, cap = StrokeCap.Round))
    }
}

@Composable
private fun ConnectingIcon() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val c = center
        val r = size.minDimension / 3
        // Scanning dots
        for (i in 0..2) {
            val angle = Math.toRadians((System.currentTimeMillis() / 10 % 360 + i * 120).toDouble())
            val x = c.x + cos(angle).toFloat() * r * 0.6f
            val y = c.y + sin(angle).toFloat() * r * 0.6f
            drawCircle(
                SurfColors.StatusConnecting.copy(alpha = 0.8f - i * 0.2f),
                r * 0.12f,
                Offset(x, y)
            )
        }
    }
}

@Composable
private fun ErrorIcon() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val c = center
        val r = size.minDimension / 3
        // X mark
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(c.x - r * 0.4f, c.y - r * 0.4f)
            lineTo(c.x + r * 0.4f, c.y + r * 0.4f)
            moveTo(c.x + r * 0.4f, c.y - r * 0.4f)
            lineTo(c.x - r * 0.4f, c.y + r * 0.4f)
        }
        drawPath(path, SurfColors.StatusError, style = Stroke(r * 0.2f, cap = StrokeCap.Round))
        drawCircle(SurfColors.StatusError.copy(alpha = 0.2f), r * 1.2f)
    }
}
