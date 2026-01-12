plugins {
    id("simprints.infra")
    id("simprints.android.library")
}

android {
    namespace = "com.simprints.infra.uibase"

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    viewBinding.enable = true
}

dependencies {
    implementation(project(":infra:logging"))

    api(project(":infra:core"))

    api(libs.androidX.core)
    api(libs.androidX.appcompat)
    api(libs.androidX.lifecycle)
    api(libs.androidX.lifecycle.scope)
    api(libs.androidX.lifecycle.livedata.ktx)

    api(libs.support.material)

    api(libs.androidX.ui.fragment)
    api(libs.androidX.ui.constraintlayout)
    api(libs.androidX.ui.coordinatorlayout)
    api(libs.androidX.ui.cardview)

    api(libs.androidX.navigation.fragment)

    api(libs.androidX.cameraX.core)
    api(libs.androidX.cameraX.lifecycle)
    api(libs.androidX.cameraX.view)
    api(libs.playServices.barcode)

    testImplementation(project(":infra:test-tools"))
}
