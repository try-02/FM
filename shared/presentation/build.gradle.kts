import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}
kotlin {
    android {
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
        buildTypes {
            release {
                ndk {
                    abiFilters += listOf("arm64-v8a")
                }
                optimization {
                    enable = true
                }
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":shared:domain"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.animation)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}
