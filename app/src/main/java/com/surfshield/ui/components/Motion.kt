package com.surfshield.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.surfshield.data.BackgroundStyle
import com.surfshield.ui.theme.LocalMotionEnabled
import com.surfshield.ui.theme.LocalMotionScale
import com.surfshield.ui.theme.SurfPalette
import kotlin.math.cos
import kotlin.math.sin

/**
 * Scale a duration by the user's motion speed preference, and collapse it to a
 * single frame when reduced motion is on.
 */
@Composable
fun motionDuration(base: Int): Int {
    if (!LocalMotionEnabled.current) return 0
    return (base * LocalMotionScale.current).toInt().coerceAtLeast(1)
}

/**
 * Backdrop for the home screen.
 *
 * AURORA drifts three wide, very low-alpha accent blobs across the background on
 * slow independent Lissajous paths. It is drawn on a Canvas rather than with
 * blurs so it stays cheap on the mid-range hardware this app targets, and it
 * freezes into a static composition when reduced motion is on.
 */
@Composable
fun SurfBackground(
    style: BackgroundStyle,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = LocalMotionEnabled.current
    val base = SurfPalette.Navy

    val transition = rememberInfiniteTransition(label = "aurora")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(motionDuration(26_000).coerceAtLeast(1), easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "drift",
    )
    val t = if (motion) drift else 0.22f

    Box(
        modifier
            .fillMaxSize()
            .background(
                when (style) {
                    BackgroundStyle.PLAIN -> Brush.verticalGradient(listOf(base, base))
                    else -> Brush.verticalGradient(
                        listOf(base, SurfPalette.Surface, base)
                    )
                }
            )
    ) {
        if (style == BackgroundStyle.AURORA) {
            val blobs = listOf(
                Triple(SurfPalette.Teal, 0.00f, 1.0f),
                Triple(SurfPalette.Indigo, 0.33f, 0.8f),
                Triple(SurfPalette.Cyan, 0.66f, 1.3f),
            )
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val radius = size.minDimension * 0.62f
                blobs.forEach { (color, phase, speed) ->
                    val a = (t * speed + phase) * 2f * Math.PI.toFloat()
                    val center = Offset(
                        x = w * (0.5f + 0.34f * cos(a)),
                        y = h * (0.42f + 0.30f * sin(a * 0.8f)),
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.16f), Color.Transparent),
                            center = center,
                            radius = radius,
                        ),
                        radius = radius,
                        center = center,
                    )
                }
            }
        }
        content()
    }
}

/**
 * Fade and lift a list item into place, offset by its index.
 *
 * Capped at twelve so a long server list does not leave the last rows waiting.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var shown by remember { mutableStateOf(false) }
    val delay = motionDuration(28) * index.coerceAtMost(12)
    LaunchedEffect(Unit) { shown = true }

    AnimatedVisibility(
        visible = shown,
        modifier = modifier,
        enter = fadeIn(tween(motionDuration(260), delayMillis = delay)) +
            slideInVertically(tween(motionDuration(320), delayMillis = delay)) { it / 4 },
    ) {
        content()
    }
}

/** Springy press feedback. Cheaper to read than a ripple on dark surfaces. */
@Composable
fun Modifier.pressScale(
    interaction: MutableInteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val motion = LocalMotionEnabled.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && motion) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pressScale",
    )
    return this.scale(scale)
}
