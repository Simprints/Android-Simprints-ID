plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    namespace = "com.simprints.feature.alert"

    buildFeatures.viewBinding = true
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":infralogging"))
    implementation(project(":infraresources"))

    api(libs.androidX.appcompat)
    api(libs.androidX.ui.constraintlayout)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Navigation
    implementation(libs.androidX.navigation.fragment)

    // Fragment
    implementation(libs.androidX.ui.fragment.kotlin)

    // UI
    api(libs.support.material)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines.test)
    testImplementation(libs.testing.robolectric.core)
    testImplementation(libs.hilt.testing)
    kaptTest(libs.hilt)

    testImplementation(libs.testing.androidX.runner)
    testImplementation(libs.testing.androidX.navigation)
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.fragment.testing) {
        exclude("androidx.test", "core")
    }

    testImplementation(libs.testing.espresso.core)
    testImplementation(libs.testing.espresso.contrib)
}
