package com.surfshield.ui

import com.surfshield.data.SurfLocation

data class SurfShieldState(
    val locations: List<SurfLocation> = emptyList(),
    val selectedLocation: SurfLocation? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val selectedProtocol: ProtocolType = ProtocolType.AMNEZIAWG,
    val isScanning: Boolean = false,
    val scanResults: List<SurfLocation> = emptyList(),
    val showLocationSheet: Boolean = false,
    val showProtocolSheet: Boolean = false,
    val showSettings: Boolean = false,
    val error: String? = null
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class ProtocolType(val displayName: String, val shortName: String, val description: String) {
    WIREGUARD("WireGuard", "WG", "Standard WireGuard protocol"),
    AMNEZIAWG("AmneziaWG", "AWG", "Obfuscated WireGuard with packet padding"),
    SHADOWSOCKS("Shadowsocks", "SS", "SOCKS5 proxy with encryption")
}
