plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}
android {
    namespace = "com.crouton.alpha.data"
    compileSdk = 34
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
            implementation(project(":shared:domain"))
            implementation(libs.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.sqldelight.runtime)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.driver.android)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.driver.native)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
sqldelight {
    databases {
        create("BasisData") {
            packageName.set("com.crouton.alpha.data.db")
        }
    }
}
