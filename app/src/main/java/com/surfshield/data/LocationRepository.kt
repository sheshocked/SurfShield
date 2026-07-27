package com.surfshield.data

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads server profiles out of the encrypted vault packaged by the build.
 *
 * The previous implementation failed on every launch for two independent
 * reasons, both hidden by a blanket catch that returned an empty list, so the
 * app showed no servers and no error:
 *
 *  - the source is a top-level JSON array, but the code asked for an object with
 *    a "locations" member;
 *  - it uses snake_case keys while the code requested camelCase through
 *    getString(), which throws when the key is absent.
 *
 * Both shapes and both naming styles are now accepted, and a malformed entry is
 * skipped individually instead of discarding the whole file.
 */
object LocationRepository {

    private const val TAG = "SurfShield/Locations"
    const val DEFAULT_PORT = 51820

    fun load(context: Context): List<SurfLocation> {
        val json = try {
            ConfigVault.readJson(context)
        } catch (e: Exception) {
            Log.e(TAG, "Could not read the profile vault", e)
            return emptyList()
        }

        val array = try {
            asArray(json)
        } catch (e: Exception) {
            Log.e(TAG, "Profile vault is not valid JSON", e)
            return emptyList()
        }

        val locations = ArrayList<SurfLocation>(array.length())
        for (i in 0 until array.length()) {
            val entry = array.optJSONObject(i) ?: continue
            runCatching { parse(entry) }
                .onSuccess { locations.add(it) }
                .onFailure { Log.w(TAG, "Skipping malformed profile at index $i", it) }
        }

        if (locations.isEmpty()) {
            Log.e(TAG, "Profile vault decrypted but contained no usable entries")
        }
        return locations
    }

    /** Accepts both a bare array and an object wrapping one. */
    private fun asArray(json: String): JSONArray {
        val trimmed = json.trim()
        return if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            val obj = JSONObject(trimmed)
            obj.optJSONArray("locations")
                ?: obj.optJSONArray("servers")
                ?: throw IllegalArgumentException("No profile array found in the vault")
        }
    }

    private fun parse(obj: JSONObject): SurfLocation {
        val ip = obj.firstString("resolved_ip", "resolvedIp", "ip", "endpoint_ip")
            ?: throw IllegalArgumentException("Profile has no IP address")

        val id = obj.firstString("id", "code", "name")
            ?: throw IllegalArgumentException("Profile has no id")

        return SurfLocation(
            id = id,
            country = obj.firstString("country") ?: "",
            city = obj.firstString("city") ?: "",
            emojiFlag = obj.firstString("emojiFlag", "emoji_flag", "flag") ?: "",
            // Kept for display only. It is never resolved: on a filtered network
            // DNS is both the slowest part of connecting and the easiest thing
            // to tamper with, so the endpoint is always a literal address.
            domain = obj.firstString("domain", "hostname") ?: "",
            ip = ip,
            port = obj.optInt("port", DEFAULT_PORT).let { if (it <= 0) DEFAULT_PORT else it },
            privateKey = obj.firstString("private_key", "privateKey")
                ?: throw IllegalArgumentException("Profile $id has no private key"),
            publicKey = obj.firstString("public_key", "publicKey")
                ?: throw IllegalArgumentException("Profile $id has no peer public key"),
            address = obj.firstString("address", "interface_address") ?: "10.14.0.2/16",
            dns = (obj.firstString("dns") ?: "").split(",").map { it.trim() }.filter { it.isNotBlank() },
            amnezia = parseAmnezia(obj),
            altIps = parseAltIps(obj),
        )
    }

    private fun parseAmnezia(obj: JSONObject): AmneziaParams = AmneziaParams(
        jc = obj.optInt("jc", 0),
        jmin = obj.optInt("jmin", 0),
        jmax = obj.optInt("jmax", 0),
        s1 = obj.optInt("s1", 0),
        s2 = obj.optInt("s2", 0),
        h1 = obj.optLong("h1", 1L),
        h2 = obj.optLong("h2", 2L),
        h3 = obj.optLong("h3", 3L),
        h4 = obj.optLong("h4", 4L),
        i1 = obj.optString("i1", ""),
        i2 = obj.optString("i2", ""),
    )

    private fun parseAltIps(obj: JSONObject): List<String> {
        val array = obj.optJSONArray("alt_ips")
            ?: obj.optJSONArray("altIps")
            ?: return emptyList()
        val out = ArrayList<String>(array.length())
        for (i in 0 until array.length()) {
            array.optString(i).takeIf { it.isNotBlank() }?.let(out::add)
        }
        return out
    }

    /** First non-blank value among [keys], tolerating either naming style. */
    private fun JSONObject.firstString(vararg keys: String): String? {
        for (key in keys) {
            val value = optString(key, "").trim()
            if (value.isNotEmpty() && value != "null") return value
        }
        return null
    }
}
