import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.library")
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

    ndkVersion =   gradleLocalProperties(rootDir).getProperty("ndk.Version")
        ?: System.getenv("ndk.Version")
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

    implementation(project(":infralogging"))
    implementation(project(":core"))
    implementation(project(":eventsystem"))
    implementation(project(":infraresources"))
    implementation(project(":infralicense"))
    implementation(project(":infralicense"))
    implementation(project(":infraimages"))

    implementation(libs.cameraView){
        exclude("androidx.exifinterface")
    }
    implementation(libs.circleImageView)
    
    implementation(libs.androidX.navigation.fragment)
    implementation(libs.androidX.navigation.ui)

    implementation(libs.playcore.core.ktx)

    // Fragment
    implementation(libs.androidX.ui.fragment)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Android X
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.cameraX.core){
        exclude("androidx.exifinterface")
    }

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

    // Koin
    androidTestImplementation(libs.testing.koin)

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
}

configurations {
    androidTestImplementation {
        // Mockk v1.1.12 and jvm 11 has the same file ValueClassSupport
        // the issue is reported here https://github.com/mockk/mockk/issues/722
        exclude("io.mockk", "mockk-agent-jvm")
    }
}
