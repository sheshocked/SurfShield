package com.surfshield.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.surfshield.ui.theme.LocalMotionEnabled
import com.surfshield.ui.theme.SurfPalette
import kotlin.math.sin

/**
 * Spring press scale modifier that gives bouncy feedback instead of a standard ripple.
 */
fun Modifier.pressScale(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val motionEnabled = LocalMotionEnabled.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && motionEnabled) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "press_scale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Living Aurora background: three slow low-alpha glowing blobs moving on Lissajous paths.
 */
@Composable
fun LivingAurora(
    modifier: Modifier = Modifier,
    primaryColor: Color = SurfPalette.Teal,
    secondaryColor: Color = SurfPalette.Cyan,
    tertiaryColor: Color = SurfPalette.Indigo
) {
    val motionEnabled = LocalMotionEnabled.current
    if (!motionEnabled) {
        // Fallback to static solid dark background
        Box(modifier.fillMaxSize())
        return
    }

    val transition = rememberInfiniteTransition(label = "aurora")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Blob 1 (Lissajous: a=3, b=2)
        val x1 = width / 2 + width * 0.25f * sin(3 * time)
        val y1 = height / 2 + height * 0.2f * sin(2 * time)
        drawBlob(Offset(x1, y1), width * 0.35f, primaryColor.copy(alpha = 0.08f))

        // Blob 2 (Lissajous: a=1, b=3)
        val x2 = width / 2 + width * 0.2f * sin(time)
        val y2 = height / 2 + height * 0.25f * sin(3 * time + 1.5f)
        drawBlob(Offset(x2, y2), width * 0.4f, secondaryColor.copy(alpha = 0.06f))

        // Blob 3 (Lissajous: a=2, b=4)
        val x3 = width / 2 + width * 0.3f * sin(2 * time + 0.5f)
        val y3 = height / 2 + height * 0.15f * sin(4 * time)
        drawBlob(Offset(x3, y3), width * 0.3f, tertiaryColor.copy(alpha = 0.07f))
    }
}

private fun DrawScope.drawBlob(center: Offset, radius: Float, color: Color) {
    drawCircle(
        brush = androidx.compose.ui.graphics.Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
