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

rootProject.name = "OptiSystemFF"

include(
    ":app",
    ":core:model",
    ":core:engine",
    ":core:diagnostics",
    ":core:games",
    ":core:session",
)
