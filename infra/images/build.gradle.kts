plugins {
    id("simprints.infra")
    id("simprints.library.room")
    id("kotlin-parcelize")
    id("simprints.testing.android")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.simprints.infra.images"
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:events"))

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)

    implementation(libs.androidX.security)
    implementation(libs.kotlin.coroutinesPlayServices)
    implementation(libs.kotlin.serialization)

    implementation(libs.retrofit.core)
}
