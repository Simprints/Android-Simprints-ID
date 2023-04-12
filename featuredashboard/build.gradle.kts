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
    namespace = "com.simprints.feature.dashboard"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":infraevents"))
    implementation(project(":infraeventsync"))
    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraimages"))
    implementation(project(":infralogin"))
    implementation(project(":infrarecentuseractivity"))

    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.ui.constraintlayout)

    implementation(libs.fuzzywuzzy.core)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Navigation
    implementation(libs.androidX.navigation.fragment)

    // Fragment
    implementation(libs.androidX.ui.fragment.kotlin)

    // UI
    implementation(libs.androidX.ui.preference)
    implementation(libs.androidX.ui.cardview)
    implementation(libs.support.material)

    implementation(libs.workManager.work)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines)
    testImplementation(libs.testing.robolectric.core)
    testImplementation(libs.testing.hilt)
    kaptTest(libs.hilt)

    testImplementation(libs.testing.androidX.runner)
    testImplementation(libs.testing.androidX.navigation)
    testImplementation(libs.testing.androidX.core)
    testImplementation(libs.testing.fragment) {
        exclude("androidx.test", "core")
    }

    testImplementation(libs.testing.espresso.core)
    testImplementation(libs.testing.espresso.contrib)
}
