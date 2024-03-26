plugins {
    id("simprints.infra")
    id("simprints.testing.android")
}

android {
    namespace = "com.simprints.infra.license"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:events"))
    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
    implementation(libs.androidX.security)
}
