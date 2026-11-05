plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}
android {
    namespace = "id.manajerberkas.kode"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    defaultConfig {
        applicationId = "id.manajerberkas.kode"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        debug {
            matchingFallbacks += listOf("release")
            ndk {
                abiFilters += listOf("arm64-v8a", "x86_64")
            }
            optimization {
                enable = true
            }
        }
        release {
            ndk {
                abiFilters += listOf("arm64-v8a")
            }
            optimization {
                enable = true
            }
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
dependencies {
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))
    implementation(project(":shared:presentation"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.compose.material3)
    implementation(libs.sqldelight.android)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
