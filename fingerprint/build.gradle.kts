plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("dagger.hilt.android.plugin")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        //testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunner = "com.simprints.fingerprint.CustomTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    buildTypes {
        getByName("release") {
            proguardFiles("proguard-rules.pro")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "1440L")
        }

        getByName("staging") {
            proguardFiles("proguard-rules.pro")
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

    buildFeatures.viewBinding = true
    namespace = "com.simprints.fingerprint"
}

repositories {
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.google.com")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://s3.amazonaws.com/repo.commonsware.com")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Simprints
    implementation(project(":core"))
    implementation(project(":infraevents"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":fingerprintmatcher"))
    implementation(project(":fingerprintscanner"))
    implementation(project(":infraconfig"))
    implementation(project(":infralogin"))
    implementation(project(":infraimages"))
    implementation(project(":infrarecentuseractivity"))
    implementation(project(":featurealert"))

    // If mock/dummy BT adapter is required test implementation can be switched to regular one
    testImplementation(project(":fingerprintscannermock"))
    //implementation(project(":fingerprintscannermock"))

    implementation(libs.retrofit.core)
    runtimeOnly(libs.jackson.core)

    // Kotlin
    runtimeOnly(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    // Android X
    implementation(libs.androidX.core)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.ui.coordinatorlayout)
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.ui.viewpager2)
    implementation(libs.androidX.navigation.fragment)
    implementation(libs.workManager.work)

    // DI
    implementation(libs.hilt)
    implementation(libs.hilt.work)
    kapt(libs.hilt.kapt)
    kapt(libs.hilt.compiler)

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
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.coroutines.test)

    // Espresso
    testImplementation(libs.testing.espresso.core)
    // explicitly depending on accessibility-test-framework to solve this espresso 3.4.0 build issue
    // https://github.com/android/android-test/issues/861
    androidTestImplementation(libs.testing.espresso.accessibility)
    testImplementation(libs.testing.espresso.intents)

    // Mocking and assertion frameworks
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)


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

    // Android X
    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestUtil(libs.testing.androidX.orchestrator)
    androidTestImplementation(libs.testing.live.data)
    androidTestImplementation(libs.testing.coroutines.test)

    // Mocking and assertion frameworks
    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)

    // Espresso
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)

    //Hilt
    androidTestImplementation(libs.hilt.testing)
    kaptAndroidTest(libs.hilt)

    // Truth
    androidTestImplementation(libs.testing.truth)

    // Robolectric
    androidTestImplementation(libs.testing.robolectric.core)
}
