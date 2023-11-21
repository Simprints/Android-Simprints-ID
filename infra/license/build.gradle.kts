plugins {
    id("simprints.infra")
    id("simprints.testing.android")
}

android {
    namespace = "com.simprints.infra.license"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
    implementation(libs.androidX.security)
}
