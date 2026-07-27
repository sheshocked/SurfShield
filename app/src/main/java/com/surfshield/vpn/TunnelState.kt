package com.surfshield.vpn

import com.surfshield.data.SurfLocation

enum class ConnectionStatus { DISCONNECTED, CONNECTING, TESTING, CONNECTED, RECONNECTING, FAILED }

/**
 * Everything the UI needs to render the connection, in one immutable snapshot.
 */
data class TunnelState(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val location: SurfLocation? = null,
    val endpoint: String? = null,
    val profile: ObfuscationProfile? = null,
    val rttMs: Int? = null,
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    val rxRateBps: Long = 0,
    val txRateBps: Long = 0,
    val connectedSinceMs: Long? = null,
    val lastHandshakeAgoMs: Long? = null,
    /** Human readable step, e.g. "Trying Balanced on 87.249.139.180". */
    val detail: String? = null,
    val error: String? = null,
) {
    val isActive: Boolean
        get() = status == ConnectionStatus.CONNECTED ||
            status == ConnectionStatus.CONNECTING ||
            status == ConnectionStatus.TESTING ||
            status == ConnectionStatus.RECONNECTING
}
