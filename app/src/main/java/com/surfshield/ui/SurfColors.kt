package com.surfshield.ui

import androidx.compose.ui.graphics.Color

object SurfColors {
    val Background = Color(0xFF070B1A)
    val Surface = Color(0xFF0E1428)
    val SurfaceElevated = Color(0xFF151D3A)
    val SurfaceVariant = Color(0xFF1A2245)
    val Primary = Color(0xFF6C63FF)
    val PrimaryDim = Color(0xFF4A42D4)
    val PrimaryGlow = Color(0xFF8B83FF)
    val OnBackground = Color(0xFFECEDFF)
    val OnSurfaceVariant = Color(0xFF7C82A6)
    val Muted = Color(0xFF2D3566)
    val StatusConnected = Color(0xFF00E676)
    val StatusConnecting = Color(0xFFFFC107)
    val StatusError = Color(0xFFFF5252)
}

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED, ERROR
}

enum class ProtocolType {
    WIREGUARD, AMNEZIAWG, SHADOWSOCKS, VLESS
}
