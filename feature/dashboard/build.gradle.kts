plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.dashboard"
}

dependencies {
    implementation(project(":infraevents"))
    implementation(project(":infraeventsync"))
    implementation(project(":infra:config"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraimages"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infrarecentuseractivity"))
    implementation(project(":feature:consent"))

    implementation(libs.fuzzywuzzy.core)

    // UI
    implementation(libs.androidX.ui.preference)
    implementation(libs.workManager.work)
}
