package com.surfshield.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.surfshield.data.ThemeMode

/**
 * Palette derived from the logo gradient: teal to cyan to indigo on a very dark
 * navy. The accents are used sparingly so the connect button stays the single
 * brightest thing on screen.
 */
object SurfPalette {
    val Teal = Color(0xFF22E1C3)
    val Cyan = Color(0xFF12B5D8)
    val Indigo = Color(0xFF3B6BF5)
    val Mint = Color(0xFF7CF5D9)

    val Danger = Color(0xFFFF5C7A)
    val Warning = Color(0xFFFFC663)
    val Success = Color(0xFF3DDC97)

    val Navy = Color(0xFF040B14)
    val NavyAmoled = Color(0xFF000000)
    val Surface = Color(0xFF0B1520)
    val SurfaceElevated = Color(0xFF122130)
    val Outline = Color(0xFF1E2F42)

    val OnBackground = Color(0xFFEAF4FF)
    val Muted = Color(0xFF8AA0B8)
}

val ConnectGradient = listOf(SurfPalette.Teal, SurfPalette.Cyan, SurfPalette.Indigo)

val brandBrush: Brush
    get() = Brush.linearGradient(ConnectGradient)

/**
 * True when animations should be suppressed. Read by every animated composable
 * so the "reduce motion" preference is honoured app-wide instead of per screen.
 */
val LocalMotionEnabled = staticCompositionLocalOf { true }

private val SurfTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.2.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
)

@Composable
fun SurfShieldTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    motionEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val amoled = themeMode == ThemeMode.AMOLED ||
        (themeMode == ThemeMode.SYSTEM && isSystemInDarkTheme())

    val scheme = darkColorScheme(
        primary = SurfPalette.Teal,
        onPrimary = SurfPalette.Navy,
        primaryContainer = SurfPalette.Cyan,
        secondary = SurfPalette.Indigo,
        tertiary = SurfPalette.Mint,
        background = if (amoled) SurfPalette.NavyAmoled else SurfPalette.Navy,
        onBackground = SurfPalette.OnBackground,
        surface = if (amoled) Color(0xFF070707) else SurfPalette.Surface,
        onSurface = SurfPalette.OnBackground,
        surfaceVariant = SurfPalette.SurfaceElevated,
        onSurfaceVariant = SurfPalette.Muted,
        outline = SurfPalette.Outline,
        error = SurfPalette.Danger,
    )

    CompositionLocalProvider(LocalMotionEnabled provides motionEnabled) {
        MaterialTheme(colorScheme = scheme, typography = SurfTypography, content = content)
    }
}
