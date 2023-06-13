plugins {
    id("simprints.infra")
    id("simprints.library.room")
    id("simprints.config.cloud")
}

android {
    namespace = "com.simprints.infra.projectsecurity"
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infraconfig"))
    implementation(project(":infraimages"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraevents"))

    implementation(libs.retrofit.core)
    implementation(libs.workManager.work)
}
