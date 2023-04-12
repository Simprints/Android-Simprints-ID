plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    namespace = "com.simprints.testtools"
}

//Required to make the mock-android library to work in a no android module.
System.setProperty("org.mockito.mock.android", "true")

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":infraresources"))

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    implementation(libs.testing.androidX.core)
    implementation(libs.androidX.multidex)
    implementation(libs.androidX.appcompat)

    api(libs.androidX.multidex)
    api(libs.androidX.appcompat)

    implementation(libs.testing.junit)
    implementation(libs.testing.mockito.inline)
    implementation(libs.testing.mockito.kotlin)
    implementation(libs.testing.mockk.core)
    implementation(libs.testing.truth)
    implementation(libs.testing.robolectric.core)
    implementation(libs.testing.coroutines)
    implementation(libs.testing.koTest.kotlin.assert)
    implementation(libs.testing.fragment.testing) {
        exclude("androidx.test", "core")
    }
    implementation(libs.testing.espresso.core)

    implementation(libs.rxJava2.android)
    implementation(libs.rxJava2.core)
}
