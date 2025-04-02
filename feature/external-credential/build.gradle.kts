plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.externalcredential"
}

dependencies {
    implementation(project(":infra:external-credential-store"))
    implementation(project(":feature:exit-form"))

    // QR scanner related
    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)
    implementation(libs.jackson.core)
    implementation(libs.playServices.barcode)
}
