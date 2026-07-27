package com.surfshield.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SurfsharkConfig(
    val id: String,                    // e.g., "nl-ams", "tr-ist"
    val country: String,               // "Netherlands", "Turkey"
    val countryCode: String,           // "NL", "TR"
    val emojiFlag: String,             // "🇳🇱", "🇹🇷"
    val domain: String,                // "nl-ams.prod.surfshark.com"
    val resolvedIp: String,            // "81.19.208.80"
    val port: Int = 51820,
    
    // WireGuard keys
    val privateKey: String,
    val publicKey: String,
    val address: String = "10.14.0.2/16",
    val dns: String = "162.252.172.57, 149.154.159.92",
    
    // AmneziaWG parameters
    val jc: Int = 3,
    val jd: Int = 5,
    val jmin: Int = 40,
    val jmax: Int = 70,
    val s1: Int = 0,
    val s2: Int = 0,
    val h1: Int = 1,
    val h2: Int = 2,
    val h3: Int = 3,
    val h4: Int = 4,
    
    // Protocol support flags
    val supportsWireGuard: Boolean = true,
    val supportsAmneziaWG: Boolean = true,
    val supportsShadowsocks: Boolean = false,
    
    // Shadowsocks/VLESS specific (if applicable)
    val ssMethod: String = "",
    val ssPassword: String = "",
    val vlessUuid: String = "",
    val vlessFlow: String = ""
) : Parcelable

enum class ProtocolType {
    WIREGUARD,
    AMNEZIAWG,
    SHADOWSOCKS,
    VLESS
}

data class ConnectionStats(
    val upload: Long = 0,
    val download: Long = 0,
    val latency: Long = 0,
    val duration: Long = 0
)

data class ServerLatencyResult(
    val config: SurfsharkConfig,
    val latencyMs: Long,
    val success: Boolean
)

