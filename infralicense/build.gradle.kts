plugins {
    id("simprints.infra")
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
