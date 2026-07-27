pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "SurfShield"

include(":app")

// AmneziaWG ships no Maven artifact: its tunnel module builds native libraries
// with CMake and reads its package name from the amneziawgPackageName property.
// It therefore has to be vendored:
//
//   git submodule add https://github.com/amnezia-vpn/amneziawg-android \
//       third_party/amneziawg-android
//   git submodule update --init --recursive
//
// The include is conditional so a clone without submodules still configures and
// reports the reason, instead of failing on an unresolvable dependency.
val awgTunnel = file("third_party/amneziawg-android/tunnel")
if (awgTunnel.exists()) {
    include(":tunnel")
    project(":tunnel").projectDir = awgTunnel
} else {
    logger.warn(
        "AmneziaWG submodule missing at ${awgTunnel.path}. " +
            "Run: git submodule update --init --recursive"
    )
}
