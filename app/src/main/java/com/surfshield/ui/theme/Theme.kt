package com.surfshield.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.surfshield.data.ThemeMode

enum class ColorPaletteName {
    MIDNIGHT, AURORA, EMBER, NORD, FOREST, MONO
}

object SurfPalette {
    // Basic colors
    var Teal = Color(0xFF22E1C3)
    var Cyan = Color(0xFF12B5D8)
    var Indigo = Color(0xFF3B6BF5)
    var Mint = Color(0xFF7CF5D9)

    var Danger = Color(0xFFFF5C7A)
    var Warning = Color(0xFFFFC663)
    var Success = Color(0xFF3DDC97)

    var Navy = Color(0xFF040B14)
    var NavyAmoled = Color(0xFF000000)
    var Surface = Color(0xFF0B1520)
    var SurfaceElevated = Color(0xFF122130)
    var Outline = Color(0xFF1E2F42)

    var OnBackground = Color(0xFFEAF4FF)
    var Muted = Color(0xFF8AA0B8)

    fun applyPalette(palette: ColorPaletteName, dark: Boolean) {
        when (palette) {
            ColorPaletteName.MIDNIGHT -> {
                Teal = Color(0xFF22E1C3)
                Cyan = Color(0xFF12B5D8)
                Indigo = Color(0xFF3B6BF5)
                Mint = Color(0xFF7CF5D9)
            }
            ColorPaletteName.AURORA -> {
                Teal = Color(0xFFD0BCFF)
                Cyan = Color(0xFFCCC2DC)
                Indigo = Color(0xFFEFB8C8)
                Mint = Color(0xFFE8DEF8)
            }
            ColorPaletteName.EMBER -> {
                Teal = Color(0xFFFF8A65)
                Cyan = Color(0xFFFFB74D)
                Indigo = Color(0xFFFF5252)
                Mint = Color(0xFFFFCC80)
            }
            ColorPaletteName.NORD -> {
                Teal = Color(0xFF88C0D0)
                Cyan = Color(0xFF81A1C1)
                Indigo = Color(0xFF5E81AC)
                Mint = Color(0xFF8FBCBB)
            }
            ColorPaletteName.FOREST -> {
                Teal = Color(0xFF81C784)
                Cyan = Color(0xFFA5D6A7)
                Indigo = Color(0xFF2E7D32)
                Mint = Color(0xFFC8E6C9)
            }
            ColorPaletteName.MONO -> {
                Teal = Color(0xFFE0E0E0)
                Cyan = Color(0xFFBDBDBD)
                Indigo = Color(0xFF757575)
                Mint = Color(0xFFEEEEEE)
            }
        }
        if (dark) {
            Navy = Color(0xFF040B14)
            Surface = Color(0xFF0B1520)
            SurfaceElevated = Color(0xFF122130)
            Outline = Color(0xFF1E2F42)
            OnBackground = Color(0xFFEAF4FF)
            Muted = Color(0xFF8AA0B8)
        } else {
            Navy = Color(0xFFF4F6F9)
            Surface = Color(0xFFFFFFFF)
            SurfaceElevated = Color(0xFFECEFF1)
            Outline = Color(0xFFCFD8DC)
            OnBackground = Color(0xFF1A237E)
            Muted = Color(0xFF546E7A)
        }
    }
}

val ConnectGradient: List<Color>
    get() = listOf(SurfPalette.Teal, SurfPalette.Cyan, SurfPalette.Indigo)

val brandBrush: Brush
    get() = Brush.linearGradient(ConnectGradient)

val LocalMotionEnabled = staticCompositionLocalOf { true }

@Composable
fun SurfShieldTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    motionEnabled: Boolean = true,
    palette: ColorPaletteName = ColorPaletteName.MIDNIGHT,
    content: @Composable () -> Unit,
) {
    val dark = themeMode == ThemeMode.DARK || themeMode == ThemeMode.AMOLED ||
            (themeMode == ThemeMode.SYSTEM && isSystemInDarkTheme())
    val amoled = themeMode == ThemeMode.AMOLED

    SurfPalette.applyPalette(palette, dark)

    val scheme = if (dark) {
        darkColorScheme(
            primary = SurfPalette.Teal,
            onPrimary = SurfPalette.Navy,
            primaryContainer = SurfPalette.Cyan,
            secondary = SurfPalette.Indigo,
            tertiary = SurfPalette.Mint,
            background = if (amoled) SurfPalette.NavyAmoled else SurfPalette.Navy,
            onBackground = SurfPalette.OnBackground,
            surface = if (amoled) Color(0xFF000000) else SurfPalette.Surface,
            onSurface = SurfPalette.OnBackground,
            surfaceVariant = SurfPalette.SurfaceElevated,
            onSurfaceVariant = SurfPalette.Muted,
            outline = SurfPalette.Outline,
            error = SurfPalette.Danger,
        )
    } else {
        lightColorScheme(
            primary = SurfPalette.Teal,
            onPrimary = SurfPalette.Navy,
            primaryContainer = SurfPalette.Cyan,
            secondary = SurfPalette.Indigo,
            tertiary = SurfPalette.Mint,
            background = SurfPalette.Navy,
            onBackground = SurfPalette.OnBackground,
            surface = SurfPalette.Surface,
            onSurface = SurfPalette.OnBackground,
            surfaceVariant = SurfPalette.SurfaceElevated,
            onSurfaceVariant = SurfPalette.Muted,
            outline = SurfPalette.Outline,
            error = SurfPalette.Danger,
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val typography = SurfFonts.getTypography(context)

    CompositionLocalProvider(LocalMotionEnabled provides motionEnabled) {
        MaterialTheme(colorScheme = scheme, typography = typography, content = content)
    }
}
