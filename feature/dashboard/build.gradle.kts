plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.dashboard"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:sync"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:images"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":feature:consent"))
    implementation(project(":feature:login"))
    implementation(project(":feature:troubleshooting"))
    // Data Generator is a test-only feature, only included in debug builds
    debugImplementation(project(":testing:data-generator"))

    implementation(libs.fuzzywuzzy.core)

    // UI
    implementation(libs.androidX.ui.preference)
    implementation(libs.workManager.work)
}
