package com.surfshield.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.surfshield.ui.ConnectionStatus
import com.surfshield.ui.SurfColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConnectionButton(
    status: ConnectionStatus,
    size: Dp = 280.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
        // Outer ring
        Canvas(modifier = Modifier.size(size)) {
            val c = Offset(size.toPx() / 2, size.toPx() / 2)
            val r = size.toPx() / 2 * 0.95f

            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(outerColor, outerColor.copy(alpha = 0.3f), Color.Transparent, outerColor),
                    center = c
                ),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(c.x - r, c.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
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
            Box(modifier = Modifier.size(size * 0.25f)) {
                when (status) {
                    ConnectionStatus.DISCONNECTED -> DisconnectedIcon()
                    ConnectionStatus.CONNECTING -> ConnectingIcon()
                    ConnectionStatus.CONNECTED -> ConnectedIcon()
                    ConnectionStatus.ERROR -> ErrorIcon()
                }
            }
        }

        // Click overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        )
    }
}

@Composable
private fun DisconnectedIcon() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val c = center
        val r = size.minDimension / 3
        drawCircle(Color.White.copy(alpha = 0.5f), r * 0.15f, Offset(c.x, c.y + r * 0.6f))
        drawArc(
            Color.White.copy(alpha = 0.5f),
            -90f, 270f, false,
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
        drawArc(
            SurfColors.StatusConnected,
            -140f, 280f, false,
            style = Stroke(width = r * 0.25f, cap = StrokeCap.Round),
            topLeft = Offset(c.x - r * 1.1f, c.y - r * 1.1f),
            size = androidx.compose.ui.geometry.Size(r * 2.2f, r * 2.2f)
        )
        val path = Path().apply {
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
        val time = System.currentTimeMillis()
        for (i in 0..2) {
            val angle = Math.toRadians((time / 10 % 360 + i * 120).toDouble())
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
        drawCircle(SurfColors.StatusError.copy(alpha = 0.2f), r * 1.2f)
        val path = Path().apply {
            moveTo(c.x - r * 0.4f, c.y - r * 0.4f)
            lineTo(c.x + r * 0.4f, c.y + r * 0.4f)
            moveTo(c.x + r * 0.4f, c.y - r * 0.4f)
            lineTo(c.x - r * 0.4f, c.y + r * 0.4f)
        }
        drawPath(path, SurfColors.StatusError, style = Stroke(r * 0.2f, cap = StrokeCap.Round))
    }
}
