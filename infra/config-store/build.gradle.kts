plugins {
    id("simprints.infra")
    id("simprints.library.protobuf")
}

android {
    namespace = "com.simprints.infra.config.store"

}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:realm"))

    implementation(libs.datastore)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
