plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}
android {
    namespace = "com.crouton.alpha.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.crouton.alpha.android"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared:presentation"))
            implementation(project(":shared:domain"))
            implementation(project(":shared:data"))
            implementation(compose.ui)
            implementation(libs.activity.compose)
        }
    }
}
