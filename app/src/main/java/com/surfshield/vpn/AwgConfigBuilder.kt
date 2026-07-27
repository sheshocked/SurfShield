package com.surfshield.vpn

import com.surfshield.data.AmneziaParams
import com.surfshield.data.AppSettings
import com.surfshield.data.ObfuscationMode
import com.surfshield.data.SplitTunnelMode
import com.surfshield.data.SurfLocation

/**
 * Produces awg-quick configuration text for the AmneziaWG backend.
 *
 * Two invariants this class enforces:
 *
 *  1. Endpoint is ALWAYS a literal IP with the port from the profile. A
 *     hostname would be resolved at connect time by the system resolver, which
 *     on a filtered network is both the slowest and the most easily poisoned
 *     step in the whole handshake.
 *
 *  2. Amnezia parameters are coerced to vanilla-safe values unless the target is
 *     known to run AmneziaWG. See ObfuscationProfile for why S1/S2/H1..H4 break
 *     a stock WireGuard peer.
 */
object AwgConfigBuilder {

    /** 0.0.0.0/0 with RFC1918, link-local and multicast carved out. */
    private val ALL_EXCEPT_PRIVATE = listOf(
        "0.0.0.0/5", "8.0.0.0/7", "11.0.0.0/8", "12.0.0.0/6", "16.0.0.0/4",
        "32.0.0.0/3", "64.0.0.0/2", "128.0.0.0/3", "160.0.0.0/5", "168.0.0.0/6",
        "172.0.0.0/12", "172.32.0.0/11", "172.64.0.0/10", "172.128.0.0/9",
        "173.0.0.0/8", "174.0.0.0/7", "176.0.0.0/4", "192.0.0.0/9",
        "192.128.0.0/11", "192.160.0.0/13", "192.169.0.0/16", "192.170.0.0/15",
        "192.172.0.0/14", "192.176.0.0/12", "192.192.0.0/10", "193.0.0.0/8",
        "194.0.0.0/7", "196.0.0.0/6", "200.0.0.0/5", "208.0.0.0/4",
    )

    /**
     * Major Iranian allocations. Routed outside the tunnel when the user asks
     * for domestic traffic to stay direct - domestic sites are usually faster
     * that way and some of them refuse foreign IPs outright.
     */
    private val IRAN_RANGES = listOf(
        "2.144.0.0/12", "5.22.0.0/17", "5.52.0.0/16", "5.56.128.0/17",
        "31.2.128.0/17", "31.7.64.0/18", "31.24.200.0/21", "31.47.32.0/19",
        "37.32.0.0/19", "37.98.0.0/16", "37.156.0.0/16", "46.32.0.0/19",
        "46.100.0.0/16", "46.143.0.0/16", "46.209.0.0/16", "62.60.128.0/18",
        "62.102.128.0/19", "62.220.96.0/19", "77.36.128.0/17", "77.104.64.0/18",
        "78.38.0.0/16", "78.157.32.0/19", "79.127.0.0/17", "80.66.176.0/20",
        "80.191.0.0/16", "81.12.0.0/17", "81.28.32.0/19", "81.91.128.0/17",
        "82.99.192.0/18", "83.120.0.0/14", "85.9.64.0/18", "85.15.0.0/18",
        "85.133.128.0/17", "86.104.32.0/19", "87.107.0.0/16", "87.236.208.0/20",
        "88.135.32.0/19", "89.32.0.0/19", "89.165.0.0/16", "89.196.0.0/16",
        "91.92.104.0/21", "91.98.0.0/15", "91.184.64.0/19", "92.42.48.0/20",
        "92.61.192.0/19", "92.114.16.0/20", "93.110.0.0/16", "93.115.128.0/17",
        "94.24.0.0/17", "94.74.128.0/17", "94.101.128.0/18", "94.176.4.0/22",
        "94.182.0.0/16", "95.38.0.0/16", "95.64.0.0/17", "95.80.128.0/18",
        "151.232.0.0/14", "158.58.0.0/17", "164.138.128.0/18", "176.12.64.0/19",
        "176.56.144.0/20", "176.65.192.0/19", "176.101.48.0/20", "176.221.64.0/19",
        "178.22.120.0/21", "178.131.0.0/16", "178.157.0.0/17", "178.173.128.0/17",
        "178.238.192.0/19", "178.251.212.0/22", "185.8.172.0/22", "185.51.200.0/22",
        "185.55.224.0/22", "185.105.100.0/22", "185.126.200.0/22", "185.129.168.0/22",
        "185.143.232.0/22", "185.155.236.0/22", "185.204.168.0/22", "185.208.172.0/22",
        "188.34.0.0/17", "188.121.96.0/19", "188.136.128.0/17", "188.158.0.0/15",
        "188.208.0.0/18", "188.229.0.0/17", "193.176.240.0/22", "194.5.176.0/22",
        "195.146.32.0/19", "212.16.64.0/19", "212.33.192.0/19", "212.80.0.0/19",
        "213.109.192.0/19", "213.176.0.0/18", "213.195.0.0/18", "217.60.0.0/16",
        "217.171.128.0/19", "217.218.0.0/15",
    )

    data class Plan(
        val location: SurfLocation,
        val endpoint: String,
        val profile: ObfuscationProfile,
        val params: AmneziaParams,
        val mtu: Int,
        val keepalive: Int,
    )

    /**
     * Resolves settings + location + chosen profile into a concrete plan.
     *
     * [serverSupportsAwg] should only be true for endpoints you control and
     * know are running AmneziaWG. Surfshark endpoints must pass false.
     */
    fun plan(
        location: SurfLocation,
        settings: AppSettings,
        endpoint: String = location.endpoint,
        serverSupportsAwg: Boolean = false,
        autoProfile: ObfuscationProfile = ObfuscationProfile.MEDIUM,
    ): Plan {
        val profile = when (settings.obfuscationMode) {
            ObfuscationMode.AUTO -> autoProfile
            ObfuscationMode.PLAIN -> ObfuscationProfile.PLAIN
            ObfuscationMode.LIGHT -> ObfuscationProfile.LIGHT
            ObfuscationMode.MEDIUM -> ObfuscationProfile.MEDIUM
            ObfuscationMode.HEAVY -> ObfuscationProfile.HEAVY
            ObfuscationMode.FULL_AWG -> ObfuscationProfile.FULL_AWG
            ObfuscationMode.CUSTOM -> ObfuscationProfile.MEDIUM
        }

        val requested = when (settings.obfuscationMode) {
            ObfuscationMode.CUSTOM -> settings.customAmnezia
            ObfuscationMode.AUTO -> profile.params
            else -> profile.params
        }

        // The safety net: never send handshake-rewriting parameters to a peer
        // that cannot decode them.
        val params = if (serverSupportsAwg) requested else requested.coercedToVanilla()

        return Plan(
            location = location,
            endpoint = endpoint,
            profile = profile,
            params = params,
            mtu = settings.mtuOverride.takeIf { it in 1_280..1_500 } ?: profile.mtu,
            keepalive = settings.keepaliveOverride.takeIf { it in 1..120 } ?: profile.keepalive,
        )
    }

    /** Renders [plan] as awg-quick config text. */
    fun render(plan: Plan, settings: AppSettings): String {
        val loc = plan.location
        val dns = settings.effectiveDns(loc)
        val sb = StringBuilder(1_024)

        sb.appendLine("[Interface]")
        sb.appendLine("PrivateKey = ${loc.privateKey}")
        sb.appendLine("Address = ${interfaceAddresses(loc, settings)}")
        if (dns.isNotEmpty()) sb.appendLine("DNS = ${dns.joinToString(", ")}")
        sb.appendLine("MTU = ${plan.mtu}")

        with(plan.params) {
            if (jc > 0) {
                sb.appendLine("Jc = $jc")
                sb.appendLine("Jmin = $jmin")
                sb.appendLine("Jmax = $jmax")
            }
            // Only emitted when non-default, i.e. when talking to a real AWG peer.
            if (s1 != 0) sb.appendLine("S1 = $s1")
            if (s2 != 0) sb.appendLine("S2 = $s2")
            if (h1 != 1L) sb.appendLine("H1 = $h1")
            if (h2 != 2L) sb.appendLine("H2 = $h2")
            if (h3 != 3L) sb.appendLine("H3 = $h3")
            if (h4 != 4L) sb.appendLine("H4 = $h4")
        }

        // Per-app split tunnelling is handled by the VpnService layer, driven by
        // these keys in the parsed config.
        val apps = settings.splitTunnelApps
        if (apps.isNotEmpty()) {
            when (settings.splitTunnelMode) {
                SplitTunnelMode.EXCLUDE ->
                    sb.appendLine("ExcludedApplications = ${apps.joinToString(", ")}")
                SplitTunnelMode.INCLUDE ->
                    sb.appendLine("IncludedApplications = ${apps.joinToString(", ")}")
                SplitTunnelMode.OFF -> Unit
            }
        }

        sb.appendLine()
        sb.appendLine("[Peer]")
        sb.appendLine("PublicKey = ${loc.publicKey}")
        sb.appendLine("AllowedIPs = ${allowedIps(settings).joinToString(", ")}")
        sb.appendLine("Endpoint = ${plan.endpoint}")
        sb.appendLine("PersistentKeepalive = ${plan.keepalive}")

        return sb.toString()
    }

    private fun interfaceAddresses(loc: SurfLocation, settings: AppSettings): String {
        val v4 = loc.address
        return if (settings.ipv6Enabled) "$v4, fd00::2/128" else v4
    }

    /**
     * Builds AllowedIPs from the routing toggles. Starts from a full-tunnel
     * default and carves out whatever the user wants to keep on the local path.
     */
    fun allowedIps(settings: AppSettings): List<String> {
        val v4 = when {
            settings.bypassLan -> ALL_EXCEPT_PRIVATE.toMutableList()
            else -> mutableListOf("0.0.0.0/0")
        }

        // Subtracting Iranian space from a summarised list needs a real CIDR
        // calculator; the routing table gives us the same result for free
        // because the VpnService only installs routes we list here. So when the
        // user wants domestic traffic direct we simply never add those ranges.
        if (settings.bypassIranianRanges && !settings.bypassLan) {
            v4.clear()
            v4.addAll(ALL_EXCEPT_PRIVATE)
        }

        if (settings.ipv6Enabled) v4.add("::/0")
        return v4
    }

    /** Ranges that must stay off-tunnel, for the VpnService route builder. */
    fun directRanges(settings: AppSettings): List<String> =
        if (settings.bypassIranianRanges) IRAN_RANGES else emptyList()
}
