package com.surfshield.data

import android.content.Context
import android.util.Base64
import com.surfshield.BuildConfig
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Reads the encrypted server profiles that the build packaged as an asset.
 *
 * The plaintext is never committed: a Gradle task encrypts it from a source that
 * lives outside git. See app/build.gradle.kts.
 *
 * What this protects and what it does not:
 *
 * - It keeps the profiles, and the private keys in them, out of the repository
 *   and out of every commit. That is the point.
 * - It does NOT keep them secret from someone holding the APK. The app has to
 *   decrypt without a server or a user secret, so the passphrase necessarily
 *   ships with it, and unpacking the APK and reading this class is enough. The
 *   xor-obfuscated BuildConfig value raises the effort a little and no more.
 *
 * A key that has been shipped in a public APK should be treated as public.
 */
object ConfigVault {

    private const val ASSET_NAME = "locations.bin"
    private const val MAGIC = "SSV1"
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val HEADER_LEN = 4

    /** @throws IllegalStateException if the asset is missing or undecryptable. */
    fun readJson(context: Context): String {
        val blob = try {
            context.assets.open(ASSET_NAME).use { it.readBytes() }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Server profiles are missing from this build. The build must be given " +
                    "secrets/locations.json or the LOCATIONS_JSON_B64 secret.",
                e,
            )
        }

        val minimum = HEADER_LEN + SALT_LEN + IV_LEN
        check(blob.size > minimum) { "Profile vault is truncated (${blob.size} bytes)." }

        val magic = String(blob, 0, HEADER_LEN, Charsets.US_ASCII)
        check(magic == MAGIC) { "Unrecognised profile vault format." }

        val salt = blob.copyOfRange(HEADER_LEN, HEADER_LEN + SALT_LEN)
        val iv = blob.copyOfRange(HEADER_LEN + SALT_LEN, minimum)
        val ciphertext = blob.copyOfRange(minimum, blob.size)

        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(deriveKey(salt), "AES"),
                GCMParameterSpec(128, iv),
            )
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            // A GCM authentication failure means the passphrase used to build
            // does not match the one compiled in - a build configuration
            // mistake rather than a corrupt file.
            throw IllegalStateException(
                "Could not decrypt the server profiles. The build passphrase does not " +
                    "match the packaged vault.",
                e,
            )
        }
    }

    private fun deriveKey(salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            .generateSecret(
                PBEKeySpec(passphrase(), salt, BuildConfig.VAULT_ITERATIONS, 256)
            )
            .encoded

    private fun passphrase(): CharArray {
        val pad = hexToBytes(BuildConfig.VAULT_PAD)
        val obfuscated = Base64.decode(BuildConfig.VAULT_P, Base64.DEFAULT)
        val raw = ByteArray(obfuscated.size) { i ->
            (obfuscated[i].toInt() xor pad[i % pad.size].toInt()).toByte()
        }
        return String(raw, Charsets.UTF_8).toCharArray()
    }

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { i ->
            ((Character.digit(hex[i * 2], 16) shl 4) or
                Character.digit(hex[i * 2 + 1], 16)).toByte()
        }
}
