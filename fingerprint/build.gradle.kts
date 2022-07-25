plugins {
    id("com.android.dynamic-feature")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
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
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildTypes {
        getByName("release") {
            proguardFiles("proguard-rules-dynamic-features.pro")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "1440L")
        }

        getByName("staging") {
            proguardFiles("proguard-rules-dynamic-features.pro")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }

        getByName("debug") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }
    packagingOptions {
        resources {
            pickFirsts += setOf("mockito-extensions/org.mockito.plugins.MockMaker")
        }
    }

    // https://github.com/mockito/mockito/issues/1376

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

    // Simprints
    implementation(project(":id"))
    implementation(project(":fingerprintmatcher"))
    implementation(project(":fingerprintscanner"))
    implementation(project(":fingerprintscannermock"))
    implementation(project(":infralogging"))
    implementation(project(":infranetwork"))

    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    // Android X
    implementation(libs.androidX.core)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.lifecycle.viewmodel)
    implementation(libs.androidX.lifecycle.livedata)
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.ui.cardview)
    implementation(libs.androidX.ui.viewpager2)
    implementation(libs.androidX.navigation.fragment)
    implementation(libs.androidX.navigation.ui)
    implementation(libs.workManager.work)

    // RxJava
    implementation(libs.rxJava2.permissions)

    implementation(libs.rxJava2.android)
    implementation(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)



    // Splitties
    implementation(libs.splitties.core)
    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)

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
    testImplementation(libs.testing.androidX.rules)

    // Espresso
    testImplementation(libs.testing.espresso.core)
    testImplementation(libs.testing.espresso.intents)

    // Mocking and assertion frameworks
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)

    // Koin
    testImplementation(libs.testing.koin)
    testImplementation(libs.testing.live.data)


    // Robolectric
    testImplementation(libs.testing.robolectric.core)

    // ######################################################
    //                      Android test
    // ######################################################

    // Simprints
    androidTestImplementation(project(":testtools")) {
        exclude("org.robolectric")
        exclude("org.jetbrains.kotlinx")
        exclude("io.mockk")
    }

    // Koin
    androidTestImplementation(libs.testing.koin)

    // Android X
    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.core)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.rules)
    androidTestUtil(libs.testing.androidX.orchestrator)
    androidTestImplementation(libs.testing.live.data)

    // Mocking and assertion frameworks
    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)

    // Espresso
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)


    // Truth
    androidTestImplementation(libs.testing.truth)
}

configurations {
    androidTestImplementation {
        // Mockk v1.1.12 and jvm 11 has the same file ValueClassSupport
        // the issue is reported here https://github.com/mockk/mockk/issues/722
        exclude("io.mockk", "mockk-agent-jvm")
    }
}
