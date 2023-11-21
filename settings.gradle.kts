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
    ":fingerprint:connect",
    ":fingerprint:capture",
    ":fingerprint:infra:scanner",
    ":fingerprint:infra:scannermock",
    ":fingerprint:infra:base-bio-sdk",
    ":fingerprint:infra:bio-sdk",
    ":fingerprint:infra:simprints-bio-sdk",
    ":fingerprint:infra:simafis-wrapper",
)

// Face modality modules
include(
    ":face:configuration",
    ":face:capture",
    ":face:infra:face-bio-sdk",
    ":face:infra:roc-wrapper",
)

// Feature modules
include(
    ":feature:orchestrator",
    ":feature:client-api",
    ":feature:login-check",
    ":feature:login",
    ":feature:fetch-subject",
    ":feature:select-subject",
    ":feature:enrol-last-biometric",
    ":feature:dashboard",
    ":feature:alert",
    ":feature:exit-form",
    ":feature:consent",
    ":feature:setup",
    ":feature:matcher",
)

// Infra modules
include(
    ":infra:core",
    ":infra:test-tools",
    ":infra:events",
    ":infra:event-sync",
    ":infra:config-store",
    ":infra:config-sync",
    ":infra:enrolment-records-sync",
    ":infra:enrolment-records-store",
    ":infra:images",
    ":infra:license",
    ":infra:logging",
    ":infra:auth-store",
    ":infra:auth-logic",
    ":infra:project-security-store",
    ":infra:network",
    ":infra:realm",
    ":infra:recent-user-activity",
    ":infra:resources",
    ":infra:security",
    ":infra:orchestrator-data",
    ":infra:ui-base",
)
