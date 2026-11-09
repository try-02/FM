plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}
android {
    namespace = "com.crouton.alpha.android"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.crouton.alpha.android"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
}
kotlin {
    androidTarget()
    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared:presentation"))
            implementation(project(":shared:domain"))
            implementation(project(":shared:data"))
            implementation(compose.ui)
        }
    }
}
