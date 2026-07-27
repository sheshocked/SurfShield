package com.surfshield.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.surfshield.data.ThemeMode
import com.surfshield.data.ThemePalette

/**
 * Every colour the app draws with.
 *
 * The previous version hardcoded these as constants on an object, which meant
 * the theme picker had nothing to change: all six screens read the same frozen
 * values. Palettes are now data, and [SurfPalette] is a live view onto the
 * active one.
 */
@Immutable
data class SurfColors(
    val accent: Color,
    val accentAlt: Color,
    val accentDeep: Color,
    val mint: Color,
    val danger: Color,
    val warning: Color,
    val success: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val outline: Color,
    val onBackground: Color,
    val muted: Color,
)

private class Spec(val dark: SurfColors, val light: SurfColors)

/**
 * Palette definitions. Each one picks three related hues for the brand gradient
 * and a neutral ramp tuned to sit under them, so nothing clashes regardless of
 * which is selected.
 */
private fun spec(palette: ThemePalette): Spec = when (palette) {
    ThemePalette.MIDNIGHT -> Spec(
        dark = SurfColors(
            accent = Color(0xFF22E1C3),
            accentAlt = Color(0xFF12B5D8),
            accentDeep = Color(0xFF3B6BF5),
            mint = Color(0xFF7CF5D9),
            danger = Color(0xFFFF5C7A),
            warning = Color(0xFFFFC663),
            success = Color(0xFF3DDC97),
            background = Color(0xFF040B14),
            surface = Color(0xFF0B1520),
            surfaceElevated = Color(0xFF122130),
            outline = Color(0xFF1E2F42),
            onBackground = Color(0xFFEAF4FF),
            muted = Color(0xFF8AA0B8),
        ),
        light = SurfColors(
            accent = Color(0xFF00A187),
            accentAlt = Color(0xFF0A82A4),
            accentDeep = Color(0xFF2C55D4),
            mint = Color(0xFF4FD2B8),
            danger = Color(0xFFD93A5C),
            warning = Color(0xFFB47500),
            success = Color(0xFF12A06A),
            background = Color(0xFFF4F8FC),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFEBF2F9),
            outline = Color(0xFFD5E1EC),
            onBackground = Color(0xFF0A1622),
            muted = Color(0xFF5A6B7D),
        ),
    )

    ThemePalette.AURORA -> Spec(
        dark = SurfColors(
            accent = Color(0xFF9F7CFF),
            accentAlt = Color(0xFFCB6BE6),
            accentDeep = Color(0xFF5B4DE0),
            mint = Color(0xFFD9C4FF),
            danger = Color(0xFFFF6B93),
            warning = Color(0xFFFFBE5C),
            success = Color(0xFF5FD9AE),
            background = Color(0xFF08060F),
            surface = Color(0xFF130E1F),
            surfaceElevated = Color(0xFF1C142C),
            outline = Color(0xFF2C2140),
            onBackground = Color(0xFFF2ECFF),
            muted = Color(0xFF9C8FB8),
        ),
        light = SurfColors(
            accent = Color(0xFF6D3BE8),
            accentAlt = Color(0xFFA63BC4),
            accentDeep = Color(0xFF4B36C9),
            mint = Color(0xFF9B7BF0),
            danger = Color(0xFFD8305F),
            warning = Color(0xFFA96A00),
            success = Color(0xFF12996E),
            background = Color(0xFFF8F5FF),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFF0EAFF),
            outline = Color(0xFFDFD4F5),
            onBackground = Color(0xFF150F22),
            muted = Color(0xFF665C7A),
        ),
    )

    ThemePalette.EMBER -> Spec(
        dark = SurfColors(
            accent = Color(0xFFFFB347),
            accentAlt = Color(0xFFFF7A59),
            accentDeep = Color(0xFFE0455E),
            mint = Color(0xFFFFD9A0),
            danger = Color(0xFFFF5C5C),
            warning = Color(0xFFFFD166),
            success = Color(0xFF64D68F),
            background = Color(0xFF100804),
            surface = Color(0xFF1B0F08),
            surfaceElevated = Color(0xFF261610),
            outline = Color(0xFF3A2416),
            onBackground = Color(0xFFFFF3E6),
            muted = Color(0xFFB39377),
        ),
        light = SurfColors(
            accent = Color(0xFFC96A00),
            accentAlt = Color(0xFFE5533D),
            accentDeep = Color(0xFFB02B47),
            mint = Color(0xFFE0A263),
            danger = Color(0xFFC62F3F),
            warning = Color(0xFF9A6600),
            success = Color(0xFF14874F),
            background = Color(0xFFFFF9F2),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFFFEFDF),
            outline = Color(0xFFF0DAC0),
            onBackground = Color(0xFF22140A),
            muted = Color(0xFF7A6552),
        ),
    )

    ThemePalette.NORD -> Spec(
        dark = SurfColors(
            accent = Color(0xFF88C0D0),
            accentAlt = Color(0xFF81A1C1),
            accentDeep = Color(0xFF5E81AC),
            mint = Color(0xFFA3D9E3),
            danger = Color(0xFFBF616A),
            warning = Color(0xFFEBCB8B),
            success = Color(0xFFA3BE8C),
            background = Color(0xFF1B212B),
            surface = Color(0xFF242B37),
            surfaceElevated = Color(0xFF2E3644),
            outline = Color(0xFF434C5E),
            onBackground = Color(0xFFECEFF4),
            muted = Color(0xFF9CA6B8),
        ),
        light = SurfColors(
            accent = Color(0xFF3E7C93),
            accentAlt = Color(0xFF5E81AC),
            accentDeep = Color(0xFF4C6A92),
            mint = Color(0xFF7FB2C4),
            danger = Color(0xFFA8404A),
            warning = Color(0xFF95762A),
            success = Color(0xFF5E8B4A),
            background = Color(0xFFF2F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFE7EDF4),
            outline = Color(0xFFD3DBE6),
            onBackground = Color(0xFF2E3440),
            muted = Color(0xFF61708A),
        ),
    )

    ThemePalette.FOREST -> Spec(
        dark = SurfColors(
            accent = Color(0xFF6EE787),
            accentAlt = Color(0xFF2FBF71),
            accentDeep = Color(0xFF1B8A5A),
            mint = Color(0xFFB7F5C5),
            danger = Color(0xFFFF6B6B),
            warning = Color(0xFFE8C468),
            success = Color(0xFF6EE787),
            background = Color(0xFF04120B),
            surface = Color(0xFF0A1C12),
            surfaceElevated = Color(0xFF122A1C),
            outline = Color(0xFF1E3D2A),
            onBackground = Color(0xFFE9FBEF),
            muted = Color(0xFF87A894),
        ),
        light = SurfColors(
            accent = Color(0xFF178C4E),
            accentAlt = Color(0xFF12A15C),
            accentDeep = Color(0xFF0B6B41),
            mint = Color(0xFF5FBE85),
            danger = Color(0xFFC33A3A),
            warning = Color(0xFF8F7118),
            success = Color(0xFF178C4E),
            background = Color(0xFFF4FBF6),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFE8F6EE),
            outline = Color(0xFFD2E7DA),
            onBackground = Color(0xFF08180F),
            muted = Color(0xFF55705F),
        ),
    )

    ThemePalette.MONO -> Spec(
        dark = SurfColors(
            accent = Color(0xFFE8EEF5),
            accentAlt = Color(0xFFB9C6D6),
            accentDeep = Color(0xFF8A9BB0),
            mint = Color(0xFFFFFFFF),
            danger = Color(0xFFFF7A85),
            warning = Color(0xFFE7C77A),
            success = Color(0xFF9CE0B4),
            background = Color(0xFF07070A),
            surface = Color(0xFF101013),
            surfaceElevated = Color(0xFF191A1E),
            outline = Color(0xFF2A2C33),
            onBackground = Color(0xFFF6F8FB),
            muted = Color(0xFF8E939C),
        ),
        light = SurfColors(
            accent = Color(0xFF1B1D21),
            accentAlt = Color(0xFF41454D),
            accentDeep = Color(0xFF6B7280),
            mint = Color(0xFF2F3238),
            danger = Color(0xFFB3303C),
            warning = Color(0xFF8A6A18),
            success = Color(0xFF2C7A52),
            background = Color(0xFFFAFAFB),
            surface = Color(0xFFFFFFFF),
            surfaceElevated = Color(0xFFF0F1F4),
            outline = Color(0xFFDDDFE4),
            onBackground = Color(0xFF111317),
            muted = Color(0xFF5C6169),
        ),
    )
}

/**
 * Resolve a palette for the requested mode. AMOLED keeps the accent hues but
 * drops the neutral ramp to true black, which is what makes it visibly
 * different from plain dark on an OLED panel.
 */
fun themeColors(palette: ThemePalette, dark: Boolean, amoled: Boolean): SurfColors {
    val s = spec(palette)
    if (!dark) return s.light
    if (!amoled) return s.dark
    return s.dark.copy(
        background = Color(0xFF000000),
        surface = Color(0xFF070709),
        surfaceElevated = Color(0xFF101014),
        outline = Color(0xFF1F2026),
    )
}

/**
 * Live view onto the active palette.
 *
 * Kept as an object with the original member names so every existing call site
 * keeps compiling, but each read now goes through snapshot state: when
 * [SurfShieldTheme] swaps the palette, everything that painted with it
 * recomposes. That is the actual fix for the theme picker doing nothing.
 */
object SurfPalette {

    internal var colors: SurfColors by mutableStateOf(spec(ThemePalette.MIDNIGHT).dark)

    val Teal: Color get() = colors.accent
    val Cyan: Color get() = colors.accentAlt
    val Indigo: Color get() = colors.accentDeep
    val Mint: Color get() = colors.mint

    val Danger: Color get() = colors.danger
    val Warning: Color get() = colors.warning
    val Success: Color get() = colors.success

    val Navy: Color get() = colors.background
    val NavyAmoled: Color = Color(0xFF000000)
    val Surface: Color get() = colors.surface
    val SurfaceElevated: Color get() = colors.surfaceElevated
    val Outline: Color get() = colors.outline

    val OnBackground: Color get() = colors.onBackground
    val Muted: Color get() = colors.muted
}

/** The three brand hues, in gradient order. Follows the active palette. */
val ConnectGradient: List<Color>
    get() = listOf(SurfPalette.Teal, SurfPalette.Cyan, SurfPalette.Indigo)

val brandBrush: Brush
    get() = Brush.linearGradient(ConnectGradient)

/** False when the user asked for reduced motion. Read by every animation. */
val LocalMotionEnabled = staticCompositionLocalOf { true }

/** Multiplies every animation duration, so 0.5 is twice as fast as default. */
val LocalMotionScale = staticCompositionLocalOf { 1f }

@Composable
fun SurfShieldTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    palette: ThemePalette = ThemePalette.MIDNIGHT,
    motionEnabled: Boolean = true,
    motionScale: Float = 1f,
    fontScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val amoled = themeMode == ThemeMode.AMOLED
    val target = themeColors(palette, dark, amoled)

    // Cross-fade rather than snap, so switching palette reads as a deliberate
    // transition instead of a flash.
    val animated = SurfColors(
        accent = fade(target.accent, motionEnabled, "accent"),
        accentAlt = fade(target.accentAlt, motionEnabled, "accentAlt"),
        accentDeep = fade(target.accentDeep, motionEnabled, "accentDeep"),
        mint = fade(target.mint, motionEnabled, "mint"),
        danger = fade(target.danger, motionEnabled, "danger"),
        warning = fade(target.warning, motionEnabled, "warning"),
        success = fade(target.success, motionEnabled, "success"),
        background = fade(target.background, motionEnabled, "background"),
        surface = fade(target.surface, motionEnabled, "surface"),
        surfaceElevated = fade(target.surfaceElevated, motionEnabled, "surfaceElevated"),
        outline = fade(target.outline, motionEnabled, "outline"),
        onBackground = fade(target.onBackground, motionEnabled, "onBackground"),
        muted = fade(target.muted, motionEnabled, "muted"),
    )

    SideEffect { SurfPalette.colors = animated }

    val scheme = if (dark) {
        darkColorScheme(
            primary = animated.accent,
            onPrimary = animated.background,
            primaryContainer = animated.accentAlt,
            onPrimaryContainer = animated.background,
            secondary = animated.accentDeep,
            tertiary = animated.mint,
            background = animated.background,
            onBackground = animated.onBackground,
            surface = animated.surface,
            onSurface = animated.onBackground,
            surfaceVariant = animated.surfaceElevated,
            onSurfaceVariant = animated.muted,
            outline = animated.outline,
            error = animated.danger,
        )
    } else {
        lightColorScheme(
            primary = animated.accent,
            onPrimary = Color.White,
            primaryContainer = animated.accentAlt,
            onPrimaryContainer = Color.White,
            secondary = animated.accentDeep,
            tertiary = animated.mint,
            background = animated.background,
            onBackground = animated.onBackground,
            surface = animated.surface,
            onSurface = animated.onBackground,
            surfaceVariant = animated.surfaceElevated,
            onSurfaceVariant = animated.muted,
            outline = animated.outline,
            error = animated.danger,
        )
    }

    val typography = surfTypography(rememberSurfFontFamily(), fontScale)

    CompositionLocalProvider(
        LocalMotionEnabled provides motionEnabled,
        LocalMotionScale provides motionScale.coerceIn(0.4f, 2f),
    ) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}

@Composable
private fun fade(target: Color, enabled: Boolean, label: String): Color =
    if (!enabled) target else animateColorAsState(target, tween(420), label = label).value
