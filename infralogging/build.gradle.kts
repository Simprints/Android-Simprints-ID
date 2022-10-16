plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("$rootDir${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {

    buildTypes {
        getByName("release") {
            extra.set("enableCrashlytics", true)
            manifestPlaceholders["firebase_performance_logcat_enabled"] = false
            manifestPlaceholders["firebase_analytics_collection_enabled"] = true
        }
        getByName("staging") {
            extra.set("enableCrashlytics", true)
            manifestPlaceholders["firebase_performance_logcat_enabled"] = false
            manifestPlaceholders["firebase_analytics_collection_enabled"] = true
        }
        getByName("debug") {
            extra.set("enableCrashlytics", false)
            manifestPlaceholders["firebase_performance_logcat_enabled"] = false
            manifestPlaceholders["firebase_analytics_collection_enabled"] = true
        }

    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

}

dependencies {
    // We specifically don't include Crashlytics, Analytics, Performance monitoring and Timber in
    // the central buildSrc module because we do not want or expect these dependencies to be used in
    // multiple modules
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.2.13")
    implementation("com.google.firebase:firebase-analytics-ktx:21.2.0")
    implementation("com.google.firebase:firebase-perf:20.1.1")

    //4.7.1 breaks realm:
    // https://github.com/realm/realm-java/issues/6153
    // https://github.com/JakeWharton/timber/issues/295
    implementation("com.jakewharton.timber:timber:5.0.1") {
        exclude("org.jetbrains", "annotations")
    }

    // Unit Tests
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.robolectric.core)

}
