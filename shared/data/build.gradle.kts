import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.sqldelight)
}
kotlin {
    android {
        namespace = "id.manajerberkas.kode.shared.data"
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
            implementation(libs.commons.compress)
        }
    }
}
sqldelight {
    databases {
        create("DatabaseAplikasi") {
            packageName.set("id.manajerberkas.kode.data.lokal")
        }
    }
}
