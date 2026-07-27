package com.surfshield.data

/**
 * AmneziaWG obfuscation parameters.
 *
 * COMPATIBILITY - read before changing defaults:
 *
 *  - jc / jmin / jmax  ->  junk packets sent BEFORE the handshake.
 *    These are SAFE against a vanilla WireGuard server (Surfshark): the server
 *    receives datagrams it cannot parse and silently drops them, then handles
 *    the real handshake normally. This is what actually defeats most DPI
 *    fingerprinting of the fixed-size WireGuard handshake.
 *
 *  - s1 / s2 / h1..h4  ->  these rewrite the handshake packets themselves
 *    (prepended junk + custom message-type headers). They REQUIRE AmneziaWG on
 *    the server side. Enabling them against a vanilla WireGuard server makes
 *    the handshake unparsable and the tunnel will never come up.
 *
 * Therefore Surfshark locations must stay vanilla-compatible, while a personal
 * AmneziaWG server can use the full parameter set.
 */
data class AmneziaParams(
    val jc: Int = 4,
    val jmin: Int = 40,
    val jmax: Int = 70,
    val s1: Int = 0,
    val s2: Int = 0,
    val h1: Long = 1,
    val h2: Long = 2,
    val h3: Long = 3,
    val h4: Long = 4,
) {
    /** True when only knobs a vanilla WireGuard server tolerates are in use. */
    val isVanillaCompatible: Boolean
        get() = s1 == 0 && s2 == 0 && h1 == 1L && h2 == 2L && h3 == 3L && h4 == 4L

    /** Strip everything a vanilla server cannot understand, keep junk packets. */
    fun coercedToVanilla(): AmneziaParams =
        if (isVanillaCompatible) this
        else copy(s1 = 0, s2 = 0, h1 = 1, h2 = 2, h3 = 3, h4 = 4)

    companion object {
        /** No obfuscation at all - plain WireGuard. */
        val OFF = AmneziaParams(jc = 0, jmin = 0, jmax = 0)

        /** Light junk, works everywhere, negligible overhead. */
        val LIGHT = AmneziaParams(jc = 3, jmin = 40, jmax = 70)

        /** Heavier junk for aggressive DPI. Still vanilla-server compatible. */
        val HEAVY = AmneziaParams(jc = 8, jmin = 60, jmax = 1000)
    }
}

/**
 * A single connectable location.
 *
 * [ip] is always a literal address. The tunnel never resolves [domain] at
 * connect time, because DNS is the first thing that gets poisoned on a
 * censored network and a blocked resolver would take the whole tunnel down.
 */
data class SurfLocation(
    val id: String,
    val country: String,
    val city: String,
    val emojiFlag: String,
    /** Original hostname. Reference/debugging only - never dialled. */
    val domain: String,
    /** Literal IP the endpoint is built from. */
    val ip: String,
    val port: Int,
    val privateKey: String,
    val publicKey: String,
    val address: String,
    val dns: List<String>,
    val amnezia: AmneziaParams,
    /** Additional IPs behind the same hostname, used by Smart Connect. */
    val altIps: List<String> = emptyList(),
) {
    val endpoint: String get() = "$ip:$port"

    val serverSupportsAwg: Boolean get() = !amnezia.isVanillaCompatible

    val displayName: String
        get() = if (city.isBlank()) country else "$country - $city"

    /** Every address worth trying for this location, best guess first. */
    fun candidateEndpoints(): List<String> =
        (listOf(ip) + altIps).filter { it.isNotBlank() }.distinct().map { "$it:$port" }
}
