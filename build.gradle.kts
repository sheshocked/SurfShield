plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.github.ben-manes.versions") version "0.53.0" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

