package com.surfshield.ui

import androidx.compose.ui.graphics.Color

// Color palette inspired by ocean/surf themes with dark modern aesthetic
object SurfColors {
    val Background = Color(0xFF0A0F1F)       // Deep navy
    val Surface = Color(0xFF131A2F)          // Dark blue surface
    val SurfaceVariant = Color(0xFF1C2545)   // Lighter surface
    val SurfaceElevated = Color(0xFF25305A)  // Elevated surface
    val Outline = Color(0xFF2D3A67)          // Subtle borders

    val Primary = Color(0xFF6C63FF)          // Vibrant purple accent
    val PrimaryVariant = Color(0xFF5A52D5)   // Darker purple
    val PrimaryGlow = Color(0xFF8B83FF)      // Glowing purple
    val Secondary = Color(0xFF00D9FF)        // Cyan accent (ocean vibe)
    val SecondaryGlow = Color(0xFF33E5FF)    // Glowing cyan

    val Success = Color(0xFF00E676)          // Green connected
    val Error = Color(0xFFFF1744)            // Red error
    val Warning = Color(0xFFFFAB00)          // Yellow warning
    val Info = Color(0xFF448AFF)             // Blue info

    val OnBackground = Color(0xFFEEF2FF)     // Light text
    val OnSurface = Color(0xFFD0D8F0)        // Muted text
    val OnSurfaceVariant = Color(0xFF8892B0) // Subdued text
    val OnPrimary = Color.White
    val Muted = Color(0xFF5C6790)           // Very muted

    // Connection status colors
    val StatusConnecting = Color(0xFFFFAB00)
    val StatusConnected = Color(0xFF00E676)
    val StatusDisconnected = Color(0xFF8892B0)
    val StatusError = Color(0xFFFF1744)
}
