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

configurations {
    androidTestImplementation {
        // Espresso 3.4.0 has a dependency conflict issues with "checker" and "protobuf-lite" dependancies
        // https://github.com/android/android-test/issues/861
        // and https://github.com/android/android-test/issues/999
        exclude("org.checkerframework", "checker")
        exclude("com.google.protobuf", "protobuf-lite")
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
    packagingOptions {
        resources.excludes.add("META-INF/LICENSE*") // remove mockk duplicated files
    }
}
repositories {
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.google.com")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://s3.amazonaws.com/repo.commonsware.com")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infralogging"))
    implementation(project(":core"))
    implementation(project(":eventsystem"))
    implementation(project(":infraresources"))
    implementation(project(":infralicense"))
    implementation(project(":infraimages"))

    implementation(libs.cameraView){
        exclude("androidx.exifinterface")
    }
    implementation(libs.circleImageView)
    
    implementation(libs.androidX.navigation.fragment)
    implementation(libs.androidX.navigation.ui)

    // DI
    implementation(libs.hilt)
    implementation(libs.hilt.work)
    kapt(libs.hilt.kapt)
    kapt(libs.hilt.compiler)

    // Fragment
    implementation(libs.androidX.ui.fragment)

    // Android X
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.cameraX.core){
        exclude("androidx.exifinterface")
    }

    // Firebase
    //implementation("com.google.firebase:firebase-perf-ktx:20.1.1")

    // Android X
    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.core)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.rules)
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
    testImplementation(libs.testing.androidX.core)
    testImplementation(libs.testing.androidX.core.testing)

    // Kotlin
    testImplementation(libs.testing.coroutines.test)

    // Navigation
    androidTestImplementation(libs.testing.navigation.testing)
    debugImplementation(libs.testing.fragment.testing){
        exclude( "androidx.test",  "core")
    }

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

configurations {
    androidTestImplementation {
        // Mockk v1.1.12 and jvm 11 has the same file ValueClassSupport
        // the issue is reported here https://github.com/mockk/mockk/issues/722
        exclude("io.mockk", "mockk-agent-jvm")
    }
}
