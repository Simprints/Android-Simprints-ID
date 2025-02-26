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
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:sync"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:images"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":feature:consent"))
    implementation(project(":feature:login"))
    implementation(project(":feature:troubleshooting"))

    implementation(libs.fuzzywuzzy.core)

    // UI
    implementation(libs.androidX.ui.preference)
    implementation(libs.workManager.work)
}
