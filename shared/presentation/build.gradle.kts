import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // Pada AGP 9.0+, blok android berada di DALAM blok kotlin:
    android {
        namespace = "id.manajerberkas.kode.shared.data"
        compileSdk = 37
        
        // 1. minSdk ditulis langsung tanpa defaultConfig
        minSdk = 30

        buildFeatures {
            compose = true
        }

        // 2. Pengganti compileOptions/sourceCompatibility di AGP 9 KMP
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

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            implementation(libs.kotlinx.coroutines.core)
        }
    }
}