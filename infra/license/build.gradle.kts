plugins {
    id("simprints.infra")
    id("simprints.testing.android")
    id("simprints.library.kotlinSerialization")
    id("simprints.library.backendApi")
}

android {
    namespace = "com.simprints.infra.license"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(libs.androidX.security)
}
