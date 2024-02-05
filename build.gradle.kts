import java.net.HttpURLConnection
import java.net.URL
import java.util.*

plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        useCommonJs()
        browser {}
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/kotlin")
        }
        val jsMain by getting {
            dependencies {
                implementation("io.github.exav:screeps-kotlin-types:1.13.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }
    }
}
