plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.camera"
}

dependencies {
    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)
    implementation(libs.playServices.barcode)
}
