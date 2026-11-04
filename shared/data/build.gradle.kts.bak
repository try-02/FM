plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget()

    // Aktifkan nanti saat masuk tahap iOS:
    // iosArm64()
    // iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:domain"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.okio)
            implementation(libs.sqldelight.runtime)
        }

    androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.documentfile)
            implementation(libs.sqldelight.android)
        }
    }
}

android {
    namespace = "id.manajerberkas.kode.shared.data"
    compileSdk = 37

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("DatabaseAplikasi") {
            packageName.set("id.manajerberkas.kode.data.lokal")
        }
    }
}