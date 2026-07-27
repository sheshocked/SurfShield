package com.surfshield.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.surfshield.R

object SurfFonts {
    fun resolve(context: Context): FontFamily {
        val res = context.resources
        val pkg = context.packageName
        val vazirId = res.getIdentifier("vazirmatn_regular", "font", pkg)
        return if (vazirId != 0) {
            FontFamily(
                Font(vazirId, FontWeight.Normal),
                Font(res.getIdentifier("vazirmatn_medium", "font", pkg), FontWeight.Medium),
                Font(res.getIdentifier("vazirmatn_semibold", "font", pkg), FontWeight.SemiBold),
                Font(res.getIdentifier("vazirmatn_bold", "font", pkg), FontWeight.Bold)
            )
        } else {
            FontFamily.SansSerif
        }
    }

    fun getTypography(context: Context, scale: Float = 1.0f): Typography {
        val family = resolve(context)
        return Typography(
            displayLarge = TextStyle(fontFamily = family, fontSize = (57 * scale).sp, fontWeight = FontWeight.Bold),
            displayMedium = TextStyle(fontFamily = family, fontSize = (45 * scale).sp, fontWeight = FontWeight.SemiBold),
            displaySmall = TextStyle(fontFamily = family, fontSize = (34 * scale).sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
            headlineLarge = TextStyle(fontFamily = family, fontSize = (32 * scale).sp, fontWeight = FontWeight.SemiBold),
            headlineMedium = TextStyle(fontFamily = family, fontSize = (28 * scale).sp, fontWeight = FontWeight.SemiBold),
            headlineSmall = TextStyle(fontFamily = family, fontSize = (22 * scale).sp, fontWeight = FontWeight.SemiBold),
            titleLarge = TextStyle(fontFamily = family, fontSize = (22 * scale).sp, fontWeight = FontWeight.Medium),
            titleMedium = TextStyle(fontFamily = family, fontSize = (16 * scale).sp, fontWeight = FontWeight.Medium),
            titleSmall = TextStyle(fontFamily = family, fontSize = (14 * scale).sp, fontWeight = FontWeight.Medium),
            bodyLarge = TextStyle(fontFamily = family, fontSize = (16 * scale).sp, fontWeight = FontWeight.Normal),
            bodyMedium = TextStyle(fontFamily = family, fontSize = (14 * scale).sp, fontWeight = FontWeight.Normal),
            bodySmall = TextStyle(fontFamily = family, fontSize = (12 * scale).sp, fontWeight = FontWeight.Normal, letterSpacing = 0.2.sp),
            labelLarge = TextStyle(fontFamily = family, fontSize = (14 * scale).sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
            labelMedium = TextStyle(fontFamily = family, fontSize = (12 * scale).sp, fontWeight = FontWeight.Medium),
            labelSmall = TextStyle(fontFamily = family, fontSize = (11 * scale).sp, fontWeight = FontWeight.Medium)
        )
    }
}
