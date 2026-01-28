plugins {
    id("simprints.infra")
    id("simprints.library.protobuf")
    id("simprints.library.kotlinSerialization")
    id("simprints.library.backendApi")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.config.store"
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:enrolment-records:realm-store"))

    implementation(libs.datastore)
}
