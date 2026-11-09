plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.library") version "8.5.2"
}
android {
    namespace = "com.crouton.alpha.domain"
    compileSdk = 37
    defaultConfig {
        minSdk = 30
    }
}
kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
