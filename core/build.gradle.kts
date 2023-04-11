plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("kapt")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    buildFeatures {
        viewBinding = true
    }
    namespace = "com.simprints.core"

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(":moduleapi"))
    api(project(":infralogging"))
    api(project(":infranetwork"))
    api(project(":infraresources"))
    api(project(":infrasecurity"))

    api(libs.androidX.appcompat)
    api(libs.androidX.ui.fragment.kotlin)
    api(libs.support.material)
    api(libs.androidX.multidex)
    api(libs.androidX.cameraX.core)

    implementation(libs.libsimprints)
    implementation(libs.androidX.cameraX.camera2)

    runtimeOnly(libs.kotlin.coroutinesAndroid)
    implementation(libs.jackson.core)
    implementation(libs.workManager.work)
    implementation(libs.kronos.kronos)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    testImplementation(project(":testtools"))
}
