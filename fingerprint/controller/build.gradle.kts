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
}

dependencies {

    // Simprints
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":fingerprint:infra:scanner"))
    implementation(project(":infra:config"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:images"))
    implementation(project(":infra:recent-user-activity"))
    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))

    implementation(project(":fingerprint:infra:bio-sdk"))

    // Kotlin
    runtimeOnly(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    // Android X
    implementation(libs.androidX.ui.viewpager2)

    testImplementation(project(":fingerprint:infra:scannermock"))
}
