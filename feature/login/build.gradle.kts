plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.login"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))

    // Integrity check related
    implementation(libs.playServices.base)

    // QR scanner related
    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)
    implementation(libs.playServices.barcode)
}
