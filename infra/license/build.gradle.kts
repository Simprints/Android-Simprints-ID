plugins {
    id("simprints.infra")
    id("simprints.testing.android")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.simprints.infra.license"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:events"))
    implementation(libs.retrofit.core)
    implementation(libs.kotlin.serialization)
    implementation(libs.androidX.security)
}
