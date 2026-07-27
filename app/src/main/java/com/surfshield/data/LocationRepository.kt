package com.surfshield.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads assets/locations.json.
 *
 * Why this file exists: the previous inline loader in MainActivity threw an
 * exception on every single launch and was wrapped in a catch that returned
 * emptyList(), so the UI silently showed no servers at all. Two independent
 * bugs caused it:
 *
 *   1. locations.json is a top-level JSON *array*, but the loader did
 *      JSONObject(json).getJSONArray("locations") - which throws immediately.
 *   2. the asset uses snake_case keys (resolved_ip, public_key, private_key)
 *      while the loader asked for camelCase with getString(), which throws on
 *      a missing key rather than returning a default.
 *
 * This implementation accepts both shapes and both naming conventions, and
 * fails per-entry instead of discarding the entire list.
 */
object LocationRepository {

    private const val ASSET = "locations.json"
    private const val DEFAULT_PORT = 51820

    fun load(context: Context): List<SurfLocation> {
        val raw = runCatching {
            context.assets.open(ASSET).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return emptyList()

        val array = asArray(raw) ?: return emptyList()

        return (0 until array.length())
            .mapNotNull { i -> runCatching { parse(array.getJSONObject(i)) }.getOrNull() }
            .filter { it.ip.isNotBlank() && it.publicKey.isNotBlank() }
            .sortedWith(compareBy({ it.country }, { it.city }))
    }

    /** Tolerates `[ ... ]` as well as `{ "locations": [ ... ] }`. */
    private fun asArray(raw: String): JSONArray? {
        val trimmed = raw.trim()
        return runCatching {
            if (trimmed.startsWith("[")) JSONArray(trimmed)
            else JSONObject(trimmed).getJSONArray("locations")
        }.getOrNull()
    }

    private fun parse(o: JSONObject): SurfLocation {
        val id = o.firstString("id", "code") ?: error("entry without id")

        val port = o.optInt("port", o.optInt("listen_port", DEFAULT_PORT))
            .takeIf { it in 1..65535 } ?: DEFAULT_PORT

        val dns = (o.firstString("dns") ?: "1.1.1.1, 1.0.0.1")
            .split(',', ' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val altIps = o.optJSONArray("alt_ips")?.let { arr ->
            (0 until arr.length()).mapNotNull { arr.optString(it, "").takeIf(String::isNotBlank) }
        } ?: emptyList()

        return SurfLocation(
            id = id,
            country = o.firstString("country") ?: "Unknown",
            city = o.firstString("city") ?: "",
            emojiFlag = o.firstString("emojiFlag", "emoji_flag", "flag") ?: "\uD83C\uDF10",
            domain = o.firstString("domain", "hostname", "host") ?: "",
            ip = o.firstString("resolved_ip", "resolvedIp", "ip", "endpoint_ip") ?: "",
            port = port,
            privateKey = o.firstString("private_key", "privateKey") ?: "",
            publicKey = o.firstString("public_key", "publicKey") ?: "",
            address = o.firstString("address") ?: "10.14.0.2/16",
            dns = dns,
            amnezia = AmneziaParams(
                jc = o.optInt("jc", 4),
                jmin = o.optInt("jmin", 40),
                jmax = o.optInt("jmax", 70),
                s1 = o.optInt("s1", 0),
                s2 = o.optInt("s2", 0),
                h1 = o.optLong("h1", 1L),
                h2 = o.optLong("h2", 2L),
                h3 = o.optLong("h3", 3L),
                h4 = o.optLong("h4", 4L),
            ),
            altIps = altIps,
        )
    }

    /** First non-blank value among [keys], or null. */
    private fun JSONObject.firstString(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { key ->
            optString(key, "").trim().takeIf { it.isNotEmpty() }
        }
}
