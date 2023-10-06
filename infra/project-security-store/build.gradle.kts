plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.infra.projectsecurity"
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))

    implementation(libs.retrofit.core)
    implementation(libs.workManager.work)
}
