import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ---------------------------------------------------------------------------
// Config vault
//
// Server profiles are never committed. At build time the plaintext is taken
// from, in order of preference:
//
//   1. secrets/locations.json          (local development, gitignored)
//   2. $LOCATIONS_JSON_B64             (base64, used by CI)
//
// and encrypted with AES-256-GCM into a generated asset, locations.bin.
//
// The passphrase ships inside the APK because the app must decrypt offline.
// That means this protects the repository, not the binary. Treat any key that
// has ever been in a published APK as public.
// ---------------------------------------------------------------------------

val vaultPadHex = "7c4f19a3d5e2b86140fa2d7b93c6e805"
val vaultIterations = 120_000
val vaultPassphrase: String =
    (project.findProperty("surfshieldVaultPassphrase") as String?)
        ?: System.getenv("SURFSHIELD_VAULT_PASSPHRASE")
        ?: "surfshield-default-vault-passphrase"

fun hexToBytes(hex: String): ByteArray =
    ByteArray(hex.length / 2) { i ->
        ((Character.digit(hex[i * 2], 16) shl 4) or Character.digit(hex[i * 2 + 1], 16)).toByte()
    }

// The passphrase is stored in BuildConfig xor-ed with a fixed pad so it is not
// a grep-able string literal in the APK. This is obfuscation, nothing more.
fun obfuscate(value: String): String {
    val pad = hexToBytes(vaultPadHex)
    val raw = value.toByteArray(Charsets.UTF_8)
    val out = ByteArray(raw.size) { i -> (raw[i].toInt() xor pad[i % pad.size].toInt()).toByte() }
    return Base64.getEncoder().encodeToString(out)
}

fun loadPlaintextProfiles(): String? {
    val local = rootProject.file("secrets/locations.json")
    if (local.exists()) return local.readText()
    val encoded = System.getenv("LOCATIONS_JSON_B64")
    if (!encoded.isNullOrBlank()) {
        return String(Base64.getDecoder().decode(encoded.trim()), Charsets.UTF_8)
    }
    return null
}

val vaultAssetDir = layout.buildDirectory.dir("generated/vaultAssets")

val encryptProfiles = tasks.register("encryptProfiles") {
    group = "surfshield"
    description = "Encrypts the server profiles into an asset. The source is never committed."
    outputs.dir(vaultAssetDir)
    outputs.upToDateWhen { false }

    doLast {
        val dir = vaultAssetDir.get().asFile
        dir.mkdirs()
        val target = File(dir, "locations.bin")

        val plaintext = loadPlaintextProfiles()
            ?: throw GradleException(
                """
                No server profiles available, so this build would produce an app with an
                empty server list.

                Provide them in one of these ways - neither is committed:

                  local:  put the JSON at secrets/locations.json
                  CI:     set the LOCATIONS_JSON_B64 repository secret to the output of
                          base64 -w0 secrets/locations.json
                """.trimIndent()
            )

        val random = SecureRandom()
        val salt = ByteArray(16).also(random::nextBytes)
        val iv = ByteArray(12).also(random::nextBytes)

        val keyBytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            .generateSecret(
                PBEKeySpec(vaultPassphrase.toCharArray(), salt, vaultIterations, 256)
            ).encoded

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(keyBytes, "AES"),
            GCMParameterSpec(128, iv),
        )
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        target.outputStream().use { out ->
            out.write("SSV1".toByteArray(Charsets.US_ASCII))
            out.write(salt)
            out.write(iv)
            out.write(ciphertext)
        }

        logger.lifecycle("Encrypted server profiles into ${target.name} (${target.length()} bytes)")
    }
}

val releaseKeystore = rootProject.file("keystore/release.jks")

android {
    namespace = "com.surfshield"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.surfshield"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "2.0.0"

        buildConfigField("String", "VAULT_P", "\"${obfuscate(vaultPassphrase)}\"")
        buildConfigField("String", "VAULT_PAD", "\"$vaultPadHex\"")
        buildConfigField("int", "VAULT_ITERATIONS", "$vaultIterations")
    }

    signingConfigs {
        create("release") {
            if (releaseKeystore.exists()) {
                storeFile = releaseKeystore
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDir(vaultAssetDir)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (releaseKeystore.exists()) {
                signingConfigs.getByName("release")
            } else {
                // Falls back so CI can still produce an installable artifact.
                // Not suitable for distribution.
                signingConfigs.getByName("debug")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }

    packaging {
        resources.excludes += setOf(
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*",
        )
        jniLibs.useLegacyPackaging = false
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

// Assets are merged before packaging, so the encryption task has to run first.
tasks.matching { it.name.startsWith("merge") && it.name.contains("Assets") }
    .configureEach { dependsOn(encryptProfiles) }

dependencies {
    // Vendored AmneziaWG backend. See settings.gradle.kts for the submodule
    // command; the check keeps configuration working without it.
    if (findProject(":tunnel") != null) {
        implementation(project(":tunnel"))
    }

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
