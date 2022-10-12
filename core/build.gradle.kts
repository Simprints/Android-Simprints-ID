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

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":moduleapi"))
    implementation(project(":infralogging"))

    api(libs.androidX.appcompat)
    implementation(libs.androidX.lifecycle.java8)
    api(libs.androidX.multidex)
    api(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.camera2)

    implementation(libs.kotlin.coroutinesAndroid)
    implementation(libs.jackson.core)

    implementation(libs.playcore.core)

    implementation(libs.kronos.kronos)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.coroutines.test)
    testImplementation(libs.testing.koTest.kotlin.assert)
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.mockk.core)
}
