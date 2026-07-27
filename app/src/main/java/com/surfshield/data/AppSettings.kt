package com.surfshield.data

import android.content.Context
import android.content.SharedPreferences
import com.surfshield.ui.theme.ColorPaletteName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SplitTunnelMode { OFF, EXCLUDE, INCLUDE }

enum class DnsPreset(val label: String, val servers: List<String>) {
    SERVER("From server config", emptyList()),
    CLOUDFLARE("Cloudflare", listOf("1.1.1.1", "1.0.0.1")),
    GOOGLE("Google", listOf("8.8.8.8", "8.8.4.4")),
    QUAD9("Quad9", listOf("9.9.9.9", "149.112.112.112")),
    ADGUARD("AdGuard (ad blocking)", listOf("94.140.14.14", "94.140.15.15")),
    SHECAN("Shecan (Iran, domestic)", listOf("178.22.122.100", "185.51.200.2")),
    CUSTOM("Custom", emptyList()),
}

enum class ObfuscationMode { AUTO, PLAIN, LIGHT, MEDIUM, HEAVY, FULL_AWG, CUSTOM }

enum class ThemeMode { SYSTEM, DARK, AMOLED }

/**
 * Single source of truth for user preferences.
 *
 * Backed by SharedPreferences and exposed as StateFlow so Compose recomposes
 * without any manual wiring. Every value has a defined default, so a fresh
 * install behaves sensibly on a filtered Iranian connection: Smart Connect on,
 * obfuscation on auto, LAN bypass on, kill switch off.
 */
class AppSettings private constructor(private val prefs: SharedPreferences) {

    companion object {
        private const val FILE = "surfshield_settings"

        @Volatile private var instance: AppSettings? = null

        fun get(context: Context): AppSettings =
            instance ?: synchronized(this) {
                instance ?: AppSettings(
                    context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
                ).also { instance = it }
            }
    }

    // ---------------------------------------------------------------- connection

    /** Rank endpoints by latency and connect to the fastest reachable one. */
    var smartConnect: Boolean by BoolPref("smart_connect", true)

    /** Probe the whole server list on launch to populate ping badges. */
    var probeOnLaunch: Boolean by BoolPref("probe_on_launch", true)

    /** Re-establish the tunnel when the device moves between Wi-Fi and mobile. */
    var reconnectOnNetworkChange: Boolean by BoolPref("reconnect_on_network_change", true)

    var autoConnectOnBoot: Boolean by BoolPref("auto_connect_on_boot", false)

    var autoConnectOnUntrustedWifi: Boolean by BoolPref("auto_connect_untrusted_wifi", false)

    /** Block all traffic while the tunnel is down instead of leaking clear. */
    var killSwitch: Boolean by BoolPref("kill_switch", false)

    var lastLocationId: String?
        get() = prefs.getString("last_location_id", null)
        set(value) = commit { putString("last_location_id", value) }

    // -------------------------------------------------------------- obfuscation

    var obfuscationMode: ObfuscationMode
        get() = prefs.getString("obfuscation_mode", null)
            ?.let { runCatching { ObfuscationMode.valueOf(it) }.getOrNull() }
            ?: ObfuscationMode.AUTO
        set(value) = commit { putString("obfuscation_mode", value.name) }

    /** Used when [obfuscationMode] is CUSTOM. */
    var customAmnezia: AmneziaParams
        get() = AmneziaParams(
            jc = prefs.getInt("awg_jc", 4),
            jmin = prefs.getInt("awg_jmin", 40),
            jmax = prefs.getInt("awg_jmax", 70),
            s1 = prefs.getInt("awg_s1", 0),
            s2 = prefs.getInt("awg_s2", 0),
            h1 = prefs.getLong("awg_h1", 1L),
            h2 = prefs.getLong("awg_h2", 2L),
            h3 = prefs.getLong("awg_h3", 3L),
            h4 = prefs.getLong("awg_h4", 4L),
        )
        set(v) = commit {
            putInt("awg_jc", v.jc); putInt("awg_jmin", v.jmin); putInt("awg_jmax", v.jmax)
            putInt("awg_s1", v.s1); putInt("awg_s2", v.s2)
            putLong("awg_h1", v.h1); putLong("awg_h2", v.h2)
            putLong("awg_h3", v.h3); putLong("awg_h4", v.h4)
        }

    // -------------------------------------------------------------------- routing

    var splitTunnelMode: SplitTunnelMode
        get() = prefs.getString("split_mode", null)
            ?.let { runCatching { SplitTunnelMode.valueOf(it) }.getOrNull() }
            ?: SplitTunnelMode.OFF
        set(value) = commit { putString("split_mode", value.name) }

    /** Package names selected for split tunnelling. */
    var splitTunnelApps: Set<String>
        get() = prefs.getStringSet("split_apps", emptySet()) ?: emptySet()
        set(value) = commit { putStringSet("split_apps", value) }

    /** Keep RFC1918 traffic on the local link (printers, NAS, router UI). */
    var bypassLan: Boolean by BoolPref("bypass_lan", true)

    /** Route Iranian address space directly so domestic sites stay fast. */
    var bypassIranianRanges: Boolean by BoolPref("bypass_ir", true)

    var ipv6Enabled: Boolean by BoolPref("ipv6_enabled", false)

    // ------------------------------------------------------------------------ dns

    var dnsPreset: DnsPreset
        get() = prefs.getString("dns_preset", null)
            ?.let { runCatching { DnsPreset.valueOf(it) }.getOrNull() }
            ?: DnsPreset.CLOUDFLARE
        set(value) = commit { putString("dns_preset", value.name) }

    var customDns: List<String>
        get() = (prefs.getString("dns_custom", "") ?: "")
            .split(',').map { it.trim() }.filter { it.isNotEmpty() }
        set(value) = commit { putString("dns_custom", value.joinToString(",")) }

    // ---------------------------------------------------------------- tunnel tuning

    /** 0 means let the active obfuscation profile decide. */
    var mtuOverride: Int by IntPref("mtu_override", 0)

    /** 0 means use the profile value. */
    var keepaliveOverride: Int by IntPref("keepalive_override", 0)

    // ------------------------------------------------------------------ appearance

    var themeMode: ThemeMode
        get() = prefs.getString("theme_mode", null)
            ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.DARK
        set(value) = commit { putString("theme_mode", value.name) }

    var colorPalette: ColorPaletteName
        get() = prefs.getString("color_palette", null)
            ?.let { runCatching { ColorPaletteName.valueOf(it) }.getOrNull() }
            ?: ColorPaletteName.MIDNIGHT
        set(value) = commit { putString("color_palette", value.name) }

    var animationsEnabled: Boolean by BoolPref("animations_enabled", true)

    var hapticFeedback: Boolean by BoolPref("haptics", true)

    /** "fa" or "en"; null follows the system locale. */
    var language: String?
        get() = prefs.getString("language", null)
        set(value) = commit { putString("language", value) }

    var showStatsInNotification: Boolean by BoolPref("notif_stats", true)

    var verboseLogging: Boolean by BoolPref("verbose_logging", false)

    // ------------------------------------------------------------------ observation

    private val revisionState = MutableStateFlow(0L)

    /** Bumps on every write so Compose can observe the whole store cheaply. */
    val revision: StateFlow<Long> = revisionState.asStateFlow()

    /** Effective DNS servers for [location], honouring the chosen preset. */
    fun effectiveDns(location: SurfLocation): List<String> = when (dnsPreset) {
        DnsPreset.SERVER -> location.dns
        DnsPreset.CUSTOM -> customDns.ifEmpty { location.dns }
        else -> dnsPreset.servers
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        revisionState.value = revisionState.value + 1
    }

    // ----------------------------------------------------------------- plumbing

    private inline fun commit(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
        revisionState.value = revisionState.value + 1
    }

    private inner class BoolPref(private val key: String, private val default: Boolean) {
        operator fun getValue(thisRef: Any?, p: Any?): Boolean = prefs.getBoolean(key, default)
        operator fun setValue(thisRef: Any?, p: Any?, value: Boolean) =
            commit { putBoolean(key, value) }
    }

    private inner class IntPref(private val key: String, private val default: Int) {
        operator fun getValue(thisRef: Any?, p: Any?): Int = prefs.getInt(key, default)
        operator fun setValue(thisRef: Any?, p: Any?, value: Int) = commit { putInt(key, value) }
    }
}
