plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
    id("simprints.testing.android")
}

android {
    namespace = "com.simprints.infra.images"

}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)

    implementation(libs.androidX.security)
    implementation(libs.kotlin.coroutinesPlayServices)

}
