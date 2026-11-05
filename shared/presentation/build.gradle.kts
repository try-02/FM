import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        // 1. Namespace khusus modul presentation
        namespace = "id.manajerberkas.kode.shared.presentation"
        compileSdk {
            version = release(37) {
                minorApiLevel = 1
            }
        }
        minSdk = 30

        withHostTest {}

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
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)

            // 1. Wajib untuk transisi ekspresif (AnimatedVisibility, expand/shrink, dsb)
            implementation(libs.compose.animation)

            // 2. Wajib untuk ikon berkas lengkap (FolderZip, Code, AudioFile, VideoFile, SdCard, dsb)
            implementation(libs.compose.material.icons.extended)

            // 3. Resource KMP (string, drawable, font multiplatform)
            implementation(libs.compose.components.resources)

            // Coroutines & Lifecycle State Collection
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}