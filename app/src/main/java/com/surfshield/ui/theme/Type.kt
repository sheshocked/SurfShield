package com.surfshield.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Font families looked for at runtime, in order of preference.
 *
 * The app deliberately does not hardcode an R.font reference: the build would
 * then refuse to compile without the files present. Instead the resource table
 * is queried by name, so dropping
 *
 *   app/src/main/res/font/vazirmatn_regular.ttf
 *   app/src/main/res/font/vazirmatn_medium.ttf
 *   app/src/main/res/font/vazirmatn_semibold.ttf
 *   app/src/main/res/font/vazirmatn_bold.ttf
 *
 * into the project is the whole installation step, and the app falls back to
 * the platform sans until then. Vazirmatn is first because it covers Persian
 * properly; Inter and the rest are Latin-only and only win if it is absent.
 */
private val PREFERRED_FAMILIES = listOf("vazirmatn", "inter", "manrope", "outfit", "rubik")

private val WEIGHT_SUFFIXES = listOf(
    "regular" to FontWeight.Normal,
    "medium" to FontWeight.Medium,
    "semibold" to FontWeight.SemiBold,
    "bold" to FontWeight.Bold,
)

/** First bundled family that has at least one weight, else the platform sans. */
fun resolveFontFamily(context: Context): FontFamily {
    val res = context.resources
    val pkg = context.packageName
    PREFERRED_FAMILIES.forEach { family ->
        val fonts = WEIGHT_SUFFIXES.mapNotNull { (suffix, weight) ->
            val id = res.getIdentifier(family + "_" + suffix, "font", pkg)
            if (id != 0) Font(id, weight) else null
        }
        if (fonts.isNotEmpty()) return FontFamily(fonts)
    }
    return FontFamily.SansSerif
}

@Composable
fun rememberSurfFontFamily(): FontFamily {
    val context = LocalContext.current
    return remember(context) { resolveFontFamily(context) }
}

/**
 * Type scale for the app.
 *
 * Tighter tracking on the large sizes and a little extra on the small ones is
 * what stops a UI built from stock defaults looking like a settings dump.
 * [scale] comes from the font size preference.
 */
fun surfTypography(family: FontFamily, scale: Float): Typography {
    val s = scale.coerceIn(0.8f, 1.4f)
    fun style(
        size: Float,
        weight: FontWeight,
        tracking: Float = 0f,
        lineHeight: Float = size * 1.35f,
    ) = TextStyle(
        fontFamily = family,
        fontSize = (size * s).sp,
        fontWeight = weight,
        letterSpacing = tracking.sp,
        lineHeight = (lineHeight * s).sp,
    )

    return Typography(
        displayLarge = style(44f, FontWeight.Bold, -1.2f),
        displayMedium = style(38f, FontWeight.Bold, -1f),
        displaySmall = style(32f, FontWeight.SemiBold, -0.7f),
        headlineLarge = style(28f, FontWeight.SemiBold, -0.5f),
        headlineMedium = style(24f, FontWeight.SemiBold, -0.4f),
        headlineSmall = style(21f, FontWeight.SemiBold, -0.2f),
        titleLarge = style(19f, FontWeight.SemiBold),
        titleMedium = style(16f, FontWeight.Medium, 0.1f),
        titleSmall = style(14f, FontWeight.Medium, 0.1f),
        bodyLarge = style(16f, FontWeight.Normal, 0.1f, 24f),
        bodyMedium = style(14f, FontWeight.Normal, 0.15f, 20f),
        bodySmall = style(12.5f, FontWeight.Normal, 0.2f, 18f),
        labelLarge = style(14f, FontWeight.SemiBold, 0.4f),
        labelMedium = style(12f, FontWeight.Medium, 0.5f),
        labelSmall = style(11f, FontWeight.Medium, 0.8f),
    )
}
