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

        maven {
            name = "RocWrapperGitHubPackages"
            url = uri("https://maven.pkg.github.com/simprints/lib-roc-wrapper")
            credentials {
                username =
                    properties.getProperty("GITHUB_USERNAME", System.getenv("GITHUB_USERNAME"))
                password = properties.getProperty("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN"))
            }
        }

        maven {
            name = "NECWrapperGitHubPackages"
            url = uri("https://maven.pkg.github.com/simprints/NEC-wrapper")
            credentials {
                username =
                    properties.getProperty("GITHUB_USERNAME", System.getenv("GITHUB_USERNAME"))
                password = properties.getProperty("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN"))
            }
        }

        maven {
            name = "SecugenWrapperGitHubPackages"
            url = uri("https://maven.pkg.github.com/simprints/secugen-wrapper")
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

// Fingerprint modality modules
include(
    ":fingerprint:connect",
    ":fingerprint:capture",
    ":fingerprint:infra:scanner",
    ":fingerprint:infra:scannermock",
    ":fingerprint:infra:base-bio-sdk",
    ":fingerprint:infra:bio-sdk",
    ":fingerprint:infra:simprints-bio-sdk",
    ":fingerprint:infra:nec-bio-sdk",
    ":fingerprint:infra:simafis-wrapper",
    ":fingerprint:infra:image-distortion-config",
)

// Face modality modules
include(
    ":face:capture",
    ":face:infra:base-bio-sdk",
    ":face:infra:bio-sdk-resolver",
    ":face:infra:roc-v1",
    ":face:infra:roc-v3",
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
    ":feature:troubleshooting",
    ":feature:alert",
    ":feature:exit-form",
    ":feature:consent",
    ":feature:setup",
    ":feature:matcher",
    ":feature:validate-subject-pool",
    ":feature:select-subject-age-group",
)

// Infra modules
include(
    ":infra:core",
    ":infra:test-tools",
    ":infra:events",
    ":infra:config-store",
    ":infra:config-sync",
    ":infra:enrolment-records:repository",
    ":infra:enrolment-records:realm-store",
    ":infra:images",
    ":infra:license",
    ":infra:logging",
    ":infra:logging-persistent",
    ":infra:auth-store",
    ":infra:auth-logic",
    ":infra:network",
    ":infra:recent-user-activity",
    ":infra:resources",
    ":infra:security",
    ":infra:orchestrator-data",
    ":infra:ui-base",
    ":infra:sync",
    ":infra:event-sync",
)
