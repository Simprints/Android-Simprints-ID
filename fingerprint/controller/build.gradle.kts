plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.fingerprint"

    defaultConfig {
        testInstrumentationRunner = "com.simprints.fingerprint.CustomTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildTypes {
        getByName("release") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "1440L")
        }

        getByName("staging") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }

        getByName("debug") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {

    // Simprints
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":fingerprint:infra:scanner"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:images"))
    implementation(project(":infra:recent-user-activity"))
    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))
    implementation(project(":fingerprint:infra:simprints-bio-sdk"))

    // If mock/dummy BT adapter is required test implementation can be switched to regular one
    testImplementation(project(":fingerprint:infra:scannermock"))
    //implementation(project(":fingerprint:infra:scannermock"))

    implementation(libs.retrofit.core)
    runtimeOnly(libs.jackson.core)

    // Kotlin
    runtimeOnly(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    // Android X
    implementation(libs.androidX.ui.viewpager2)
    implementation(libs.workManager.work)
}
