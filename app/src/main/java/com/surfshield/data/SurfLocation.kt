package com.surfshield.data

data class SurfLocation(
    val id: String,
    val country: String,
    val emojiFlag: String,
    val city: String = "",
    val domain: String,
    val resolvedIp: String,
    val publicKey: String,
    val privateKey: String,
    val address: String = "10.14.0.2/16",
    val dns: String = "162.252.172.57, 149.154.159.92"
)
