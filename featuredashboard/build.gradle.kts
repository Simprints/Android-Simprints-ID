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
    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraimages"))
    implementation(project(":infralogin"))
    implementation(project(":infrarecentuseractivity"))

    implementation(libs.fuzzywuzzy.core)

    // UI
    implementation(libs.androidX.ui.preference)
    implementation(libs.workManager.work)
}
