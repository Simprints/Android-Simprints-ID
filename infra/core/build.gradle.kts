plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.core"
}

dependencies {
    api(project(":infra:logging"))
    api(project(":infra:network"))
    api(project(":infra:resources"))
    api(project(":infra:security"))

    api(libs.androidX.appcompat)
    api(libs.androidX.multidex)
    api(libs.androidX.annotation.annotation)
    api(libs.androidX.lifecycle.livedata.ktx)
    api(libs.androidX.lifecycle.process)

    api(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.camera2)

    implementation(libs.libsimprints)

    runtimeOnly(libs.kotlin.coroutinesAndroid)

    implementation(libs.jackson.core)
    implementation(libs.workManager.work)
    implementation(libs.kronos.kronos)
    implementation(libs.tink.core)

    testImplementation(project(":infra:test-tools"))
}
