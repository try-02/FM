plugins {
    alias(libs.plugins.kotlin.multiplatform)
}
kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:presentation"))
            implementation(project(":shared:domain"))
            implementation(project(":shared:data"))
        }
    }
}
