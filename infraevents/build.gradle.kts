plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.infra.events"
}

dependencies {
    implementation(project(":infraconfig"))
    implementation(project(":infra:auth-store"))

    implementation(libs.jackson.core)

    implementation(libs.workManager.work)

    androidTestImplementation(project(":infraenrolmentrecords"))
}

