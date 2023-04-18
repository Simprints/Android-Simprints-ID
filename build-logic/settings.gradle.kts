dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("libs") {
            from(files("..${File.separator}gradle${File.separator}libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
