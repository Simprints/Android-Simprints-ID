plugins {
    id("simprints.infra")
    id("simprints.library.room")
    id("simprints.testing.android")
}

android {
    namespace = "com.simprints.infra.eventsync"

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
    implementation(project(":face:capture"))
    implementation(project(":fingerprint:capture"))
    implementation(project(":infra:images"))
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:recent-user-activity"))

    implementation(libs.workManager.work)
    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
