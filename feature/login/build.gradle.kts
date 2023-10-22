plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
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
    implementation(libs.jackson.core)
    implementation(libs.playServices.barcode)
    // For concurrent.ListenableFuture needed by camera Apis
    implementation(libs.google.guava)

}
