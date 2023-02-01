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
    api(project(":core"))
    api(project(":eventsystem"))
    api(project(":infraconfig"))
    api(project(":infraenrolmentrecords"))
    api(project(":infraimages"))
    api(project(":infralogin"))
    implementation(project(":infralogging"))
    api(project(":infrarecentuseractivity"))
    implementation(project(":infraresources"))

    api(libs.androidX.appcompat)
    api(libs.androidX.ui.constraintlayout)

    implementation(libs.fuzzywuzzy.core)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Navigation
    implementation(libs.androidX.navigation.fragment)

    // Fragment
    implementation(libs.androidX.ui.fragment.kotlin)

    // UI
    api(libs.androidX.ui.preference)
    api(libs.androidX.ui.cardview)
    api(libs.support.material)

    implementation(libs.workManager.work)

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
