rootProject.name = "CroutonAlpha"
include(":appAndroid")
include(":appIos")
include(":shared:data")
include(":shared:domain")
include(":shared:presentation")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
