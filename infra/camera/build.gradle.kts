plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.camera"

    viewBinding.enable = true
}

dependencies {
    implementation(libs.support.material)
    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)

    // Exported so that dependant modules have access to PreviewView
    api(libs.androidX.cameraX.view)

    implementation(libs.playServices.barcode)
}
