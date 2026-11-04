pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ManajerBerkasKode"

include(":shared:domain")
include(":shared:data")
include(":shared:presentation")
include(":androidApp")