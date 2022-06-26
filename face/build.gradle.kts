import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.dynamic-feature")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("org.sonarqube")
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

    ndkVersion = gradleLocalProperties(rootDir).getProperty("ndk.Version")

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}
repositories {
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.google.com")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://s3.amazonaws.com/repo.commonsware.com")
}

dependencies {
    // https://issuetracker.google.com/issues/132906456
    // When Unit tests are launched in CL, the classes.jar for the base module is not included in the final testing classes.jar file.
    // So the tests that have references to the base module fail with java.lang.NoClassDefFoundError exceptions.
    // The following line includes the base module classes.jar into the final one.
    // To run unit tests from CL: ./gradlew fingerprint:test
    testRuntimeOnly(
        fileTree(
            mapOf(
                "include" to listOf("**/*.jar"),
                "dir" to "../id/build/intermediates/app_classes/"
            )
        )
    )

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":id"))
    implementation(project(":logging"))

    implementation(libs.cameraView)
    implementation(libs.circleImageView)

    // Retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.logging)
    implementation(libs.retrofit.okhttp)
    implementation(libs.retrofit.converterScalars)

    // Fragment
    implementation(libs.androidX.ui.fragment)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Android X
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.multidex)

    // Android X
    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.monitor)
    androidTestImplementation(libs.testing.androidX.core)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.runner)
    androidTestImplementation(libs.testing.androidX.rules)
    androidTestUtil(libs.testing.androidX.orchestrator)

    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)
    androidTestImplementation(libs.testing.objenesis.core)
    androidTestImplementation(libs.testing.truth)

    // Espresso
    androidTestImplementation(libs.testing.androidX.uiAutomator)
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)
    androidTestImplementation(libs.testing.espresso.barista) {
        exclude("com.android.support")
        exclude("com.google.code.findbugs")
        exclude("org.jetbrains.kotlin")
        exclude("com.google.guava")
    }

    // Koin
    androidTestImplementation(libs.testing.koin)

    // ######################################################
    //                      Unit test
    // ######################################################

    // Simprints
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.junit) {
        exclude("com.android.support")
    }

    // Android X
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.androidX.core)
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.androidX.runner)

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
}

configurations {
    androidTestImplementation {
        // Mockk v1.1.12 and jvm 11 has the same file ValueClassSupport
        // the issue is reported here https://github.com/mockk/mockk/issues/722
        exclude("io.mockk", "mockk-agent-jvm")
    }
}
