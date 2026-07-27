package com.surfshield.vpn

import android.content.Context
import com.surfshield.data.AmneziaParams

/**
 * A single obfuscation strategy.
 *
 * [requiresAwgServer] is the critical flag. AmneziaWG splits its parameters in
 * two groups:
 *
 *  - Jc / Jmin / Jmax add extra junk datagrams *around* an otherwise untouched
 *    WireGuard handshake. A vanilla server drops them and proceeds normally, so
 *    these work against Surfshark and any stock wg server.
 *  - S1 / S2 / H1..H4 change the handshake packets and their message-type
 *    headers. Only an AmneziaWG server can decode them.
 *
 * Mixing the second group with a vanilla endpoint produces a tunnel that never
 * completes a handshake, which is the single most common misconfiguration when
 * people copy AmneziaWG parameters onto a commercial VPN provider.
 */
enum class ObfuscationProfile(
    val key: String,
    val label: String,
    val summary: String,
    val params: AmneziaParams,
    val mtu: Int,
    val keepalive: Int,
    val requiresAwgServer: Boolean,
) {
    PLAIN(
        key = "plain",
        label = "Plain WireGuard",
        summary = "Fastest. No obfuscation - use on an unfiltered network.",
        params = AmneziaParams.OFF,
        mtu = 1420,
        keepalive = 25,
        requiresAwgServer = false,
    ),
    LIGHT(
        key = "light",
        label = "Light junk",
        summary = "A few junk packets before the handshake. Almost free.",
        params = AmneziaParams(jc = 3, jmin = 40, jmax = 70),
        mtu = 1400,
        keepalive = 25,
        requiresAwgServer = false,
    ),
    MEDIUM(
        key = "medium",
        label = "Balanced",
        summary = "More junk plus a smaller MTU. Good default for Iranian ISPs.",
        params = AmneziaParams(jc = 5, jmin = 50, jmax = 300),
        mtu = 1360,
        keepalive = 20,
        requiresAwgServer = false,
    ),
    HEAVY(
        key = "heavy",
        label = "Aggressive",
        summary = "Heavy junk and frequent keepalive for hostile DPI.",
        params = AmneziaParams(jc = 8, jmin = 60, jmax = 1000),
        mtu = 1280,
        keepalive = 15,
        requiresAwgServer = false,
    ),
    FULL_AWG(
        key = "full_awg",
        label = "Full AmneziaWG",
        summary = "Rewrites handshake headers. Requires an AmneziaWG server.",
        params = AmneziaParams(
            jc = 6, jmin = 60, jmax = 800,
            s1 = 90, s2 = 120,
            h1 = 1_500_000_000L, h2 = 1_600_000_000L,
            h3 = 1_700_000_000L, h4 = 1_800_000_000L,
        ),
        mtu = 1280,
        keepalive = 15,
        requiresAwgServer = true,
    );

    companion object {
        fun byKey(key: String?): ObfuscationProfile? = entries.firstOrNull { it.key == key }

        /**
         * Escalation order used by auto mode. Cheap first: a user on a clean
         * connection should never pay for obfuscation they do not need.
         */
        fun ladder(serverSupportsAwg: Boolean): List<ObfuscationProfile> =
            entries.filter { serverSupportsAwg || !it.requiresAwgServer }
    }
}

/**
 * Remembers which profile actually worked, keyed by a coarse network
 * fingerprint, so the app adapts to each user's ISP instead of shipping one
 * global guess.
 *
 * The fingerprint is deliberately coarse (transport + operator + a server tag)
 * so it survives IP churn while still separating "home ADSL" from "mobile
 * data", which behave very differently on filtered networks.
 */
class AutoTuner(context: Context) {

    private val prefs =
        context.getSharedPreferences("surfshield_autotune", Context.MODE_PRIVATE)

    fun remember(fingerprint: String, profile: ObfuscationProfile) {
        prefs.edit()
            .putString(keyFor(fingerprint), profile.key)
            .putLong("${keyFor(fingerprint)}.at", System.currentTimeMillis())
            .apply()
    }

    fun recall(fingerprint: String): ObfuscationProfile? =
        ObfuscationProfile.byKey(prefs.getString(keyFor(fingerprint), null))

    fun forget(fingerprint: String) {
        prefs.edit().remove(keyFor(fingerprint)).remove("${keyFor(fingerprint)}.at").apply()
    }

    fun clear() = prefs.edit().clear().apply()

    /**
     * Profiles to try, best-known first, then the rest of the ladder.
     */
    fun plan(fingerprint: String, serverSupportsAwg: Boolean): List<ObfuscationProfile> {
        val ladder = ObfuscationProfile.ladder(serverSupportsAwg)
        val known = recall(fingerprint)?.takeIf { it in ladder } ?: return ladder
        return listOf(known) + ladder.filter { it != known }
    }

    private fun keyFor(fingerprint: String) = "profile.$fingerprint"
}
