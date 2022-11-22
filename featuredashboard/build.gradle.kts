plugins {
    id("com.android.library")
    id("kotlin-parcelize")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    buildFeatures.viewBinding = true
    testOptions.unitTests.isIncludeAndroidResources = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":eventsystem"))
    implementation(project(":infraconfig"))
    implementation(project(":infralogin"))
    implementation(project(":infralogging"))
    implementation(project(":infrarecentuseractivity"))
    implementation(project(":infraresources"))

    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.appcompat.resource)
    implementation(libs.androidX.ui.constraintlayout)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Navigation
    implementation(libs.androidX.navigation.fragment)
    implementation(libs.androidX.navigation.ui)

    // Fragment
    implementation(libs.androidX.activity)
    implementation(libs.androidX.ui.fragment.kotlin)

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
    debugImplementation(libs.testing.fragment.testing) {
        exclude("androidx.test", "core")
    }

    testImplementation(libs.testing.espresso.core)
}
