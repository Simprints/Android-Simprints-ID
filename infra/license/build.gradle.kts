plugins {
    id("simprints.infra")
    id("simprints.testing.android")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.infra.license"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:events"))
    implementation(libs.retrofit.core)
    implementation(libs.androidX.security)
}
