plugins {
    id("simprints.infra")
    id("simprints.android.library")
}

android {
    namespace = "com.simprints.infra.camera"

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    api(project(":infra:logging"))

    api(libs.androidX.lifecycle)
    api(libs.androidX.lifecycle.scope)
    api(libs.androidX.cameraX.view)
    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)

    testImplementation(project(":infra:test-tools"))
}
