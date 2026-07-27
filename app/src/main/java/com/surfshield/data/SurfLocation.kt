package com.surfshield.data

import org.json.JSONObject

data class SurfLocation(
    val id: String,
    val country: String,
    val emojiFlag: String,
    val city: String,
    val domain: String,
    val resolvedIp: String,
    val port: Int,
    val privateKey: String,
    val publicKey: String,
    val address: String,
    val dns: String,
    val jc: Int = 3,
    val jd: Int = 5,
    val jmin: Int = 40,
    val jmax: Int = 70,
    val s1: Int = 0,
    val s2: Int = 0,
    val h1: Int = 1,
    val h2: Int = 2,
    val h3: Int = 3,
    val h4: Int = 4
) {
    val displayName: String get() = "$emojiFlag $country • $city"
    val protocolLabel: String get() = "AWG+WG"

    companion object {
        fun fromJson(json: JSONObject): SurfLocation {
            return SurfLocation(
                id = json.getString("id"),
                country = json.getString("country"),
                emojiFlag = json.getString("emojiFlag"),
                city = json.getString("city"),
                domain = json.getString("domain"),
                resolvedIp = json.getString("resolved_ip"),
                port = json.getInt("port"),
                privateKey = json.getString("private_key"),
                publicKey = json.getString("public_key"),
                address = json.getString("address"),
                dns = json.getString("dns"),
                jc = json.optInt("jc", 3),
                jd = json.optInt("jd", 5),
                jmin = json.optInt("jmin", 40),
                jmax = json.optInt("jmax", 70),
                s1 = json.optInt("s1", 0),
                s2 = json.optInt("s2", 0),
                h1 = json.optInt("h1", 1),
                h2 = json.optInt("h2", 2),
                h3 = json.optInt("h3", 3),
                h4 = json.optInt("h4", 4)
            )
        }
    }
}

class LocationRepository(private val jsonString: String) {
    private var _locations: List<SurfLocation>? = null

    fun getLocations(): List<SurfLocation> {
        if (_locations == null) {
            _locations = parseLocations(jsonString)
        }
        return _locations!!
    }

    fun findById(id: String): SurfLocation? = getLocations().find { it.id == id }

    fun findByResolvedIp(ip: String): SurfLocation? = getLocations().find { it.resolvedIp == ip }

    private fun parseLocations(json: String): List<SurfLocation> {
        val list = mutableListOf<SurfLocation>()
        try {
            val array = org.json.JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(SurfLocation.fromJson(array.getJSONObject(i)))
            }
        } catch (e: Exception) {
            android.util.Log.e("LocationRepo", "Error parsing locations", e)
        }
        return list
    }
}
