pluginManagement {
    includeBuild("build-logic")

    repositories {
        maven(url = "https://repo1.maven.org/maven2/")

        google()
        mavenCentral()
        gradlePluginPortal()
    }
}


// Utility to get local properties from file
val properties = File(rootDir, "local.properties").inputStream().use {
    java.util.Properties().apply { load(it) }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    versionCatalogs {
        create("libs") {
            from(files("build-logic${File.separator}libs.versions.toml"))
        }
    }

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://s3.amazonaws.com/repo.commonsware.com")

        maven {
            name = "SimMatcherGitHubPackages"
            url = uri("https://maven.pkg.github.com/simprints/lib-android-simmatcher")
            credentials {
                username =
                    properties.getProperty("GITHUB_USERNAME", System.getenv("GITHUB_USERNAME"))
                password = properties.getProperty("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN"))
            }
        }

    }
}

rootProject.name = "android-simprints-id"

// Main application module
include(":id")

//Fingerprint modality modules
include(
    ":fingerprint:controller",
    ":fingerprint:scanner",
    ":fingerprint:scannermock",
    ":fingerprint:infra:matcher",
)

// Modules to be refactored
include(
    ":face",
    ":clientapi",
    ":moduleapi",
)

// Feature modules
include(
    ":feature:login",
    ":feature:fetch-subject",
    ":feature:select-subject",
    ":featuredashboard",
    ":featurealert",
    ":featureexitform",
    ":featureconsent",
)

// Infra modules
include(
    ":infraevents",
    ":infraeventsync",
    ":infraconfig",
    ":infraenrolmentrecords",
    ":infraimages",
    ":infralicense",
    ":infralogging",
    ":infra:auth-store",
    ":infra:auth-logic",
    ":infranetwork",
    ":infrarealm",
    ":infrarecentuseractivity",
    ":infraresources",
    ":infrasecurity",
    ":infrauibase",
    ":infrafacebiosdk",
    ":infrarocwrapper",
)

// Tooling modules
include(
    ":core",
    ":testtools",
)
