plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.core"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    viewBinding.enable = true
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    api(project(":moduleapi"))
    api(project(":infralogging"))
    api(project(":infranetwork"))
    api(project(":infraresources"))
    api(project(":infrasecurity"))

    api(libs.androidX.appcompat)
    api(libs.androidX.multidex)
    api(libs.androidX.annotation.annotation)
    api(libs.androidX.lifecycle.livedata.ktx)

    api(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.camera2)

    implementation(libs.libsimprints)

    runtimeOnly(libs.kotlin.coroutinesAndroid)

    implementation(libs.jackson.core)
    implementation(libs.workManager.work)
    implementation(libs.kronos.kronos)

    testImplementation(project(":testtools"))
}
