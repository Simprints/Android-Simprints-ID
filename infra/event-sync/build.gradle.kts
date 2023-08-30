plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.infra.eventsync"

    buildTypes {
        getByName("release") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
        }
        getByName("staging") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }

    sourceSets {
        // Adds exported room schema location as test app assets.
        getByName("debug") {
            assets.srcDirs("$projectDir/schemas")
        }
        getByName("test") {
            java.srcDirs("$projectDir/src/debug")
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:config"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":infra:project-security-store"))
    implementation(project(":infra:recent-user-activity"))

    implementation(libs.workManager.work)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
