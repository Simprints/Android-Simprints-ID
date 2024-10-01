plugins {
    id("simprints.infra")
    id("simprints.library.protobuf")
}

android {
    namespace = "com.simprints.infra.recent.user.activity"
}

dependencies {
    implementation(libs.datastore)
}
