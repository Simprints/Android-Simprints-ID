plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.fingerprint.infra.imagedistortionconfig"
}
dependencies {
    implementation(libs.workManager.work)

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
}
