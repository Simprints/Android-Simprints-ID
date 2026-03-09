plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("simprints.library.room")
    id("simprints.testing.unit")
}

android {
    namespace = "com.simprints.infra.aichat"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:logging-persistent"))
    implementation(project(":infra:network"))
    implementation(project(":infra:serialization"))

    implementation(libs.firebase.ai)
}
