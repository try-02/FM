import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        // 1. Sesuaikan namespace khusus presentation
        namespace = "id.manajerberkas.kode.shared.presentation"
        compileSdk {
            version = release(37) {
            minorApiLevel = 1
            }
        }
        minSdk = 30

        // buildFeatures { compose = true } SUDAH DIHAPUS

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    // Aktifkan nanti saat masuk tahap iOS:
    // iosArm64()
    // iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:domain"))

            // Dependensi Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            implementation(libs.kotlinx.coroutines.core)
        }
    }
}