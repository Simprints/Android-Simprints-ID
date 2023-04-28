import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("org.sonarqube")
    id("dagger.hilt.android.plugin")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

sonarqube {
    properties {
        property("sonar.sources", "src/main/java")
    }
}

android {

    ndkVersion =   gradleLocalProperties(rootDir).getProperty("ndk.Version")
        ?: System.getenv("ndk.Version")
    defaultConfig {
        testInstrumentationRunner = "com.simprints.face.CustomTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    externalNativeBuild {
        ndkBuild.path("jni/Application.mk")
    }

    buildFeatures.viewBinding = true

    namespace = "com.simprints.face"
}
repositories {
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.google.com")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://s3.amazonaws.com/repo.commonsware.com")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraevents"))
    implementation(project(":infralicense"))
    implementation(project(":infraimages"))
    implementation(project(":featurealert"))

    implementation(libs.cameraView)
    implementation(libs.circleImageView)

    implementation(libs.androidX.navigation.fragment)
    implementation(libs.androidX.appcompat)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Fragment
    implementation(libs.androidX.ui.fragment)

    // Android X
    implementation(libs.androidX.ui.constraintlayout)
    runtimeOnly(libs.androidX.cameraX.core)

    // Firebase
    //implementation("com.google.firebase:firebase-perf-ktx:20.1.1")

    // Android X
    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestUtil(libs.testing.androidX.orchestrator)

    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)
    androidTestImplementation(libs.testing.truth)

    // Espresso
    androidTestImplementation(libs.testing.androidX.uiAutomator)
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)

    // ######################################################
    //                      Unit test
    // ######################################################

    // Simprints
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.junit)

    // Android X
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.androidX.core.testing)

    // Kotlin
    testImplementation(libs.testing.coroutines.test)

    // Navigation
    androidTestImplementation(libs.testing.navigation.testing)
    androidTestImplementation(libs.testing.fragment.testing)
    // Mockk
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.robolectric.core)

    // ######################################################
    //                      Android test
    // ######################################################

    //Hilt
    androidTestImplementation(libs.hilt.testing)
    kaptAndroidTest(libs.hilt)

    // Roboelectic
    androidTestImplementation(libs.testing.robolectric.core)
}
