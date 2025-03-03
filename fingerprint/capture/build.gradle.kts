plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.fingerprint.capture"

    defaultConfig {
        testInstrumentationRunner = "com.simprints.fingerprint.CustomTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }
}

dependencies {

    // Simprints
    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":fingerprint:infra:scanner"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:images"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))

    implementation(project(":fingerprint:connect"))
    implementation(project(":fingerprint:infra:bio-sdk"))

    // Kotlin
    runtimeOnly(libs.kotlin.reflect)

    // Android X
    implementation(libs.androidX.ui.viewpager2)
    implementation(libs.androidX.ui.preference)

    testImplementation(project(":fingerprint:infra:scannermock"))
}
