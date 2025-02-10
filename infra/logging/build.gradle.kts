plugins {
    id("simprints.android.library")
}

android {
    namespace = "com.simprints.infra.logging"

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
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.firebase.crashlytics)
    api(libs.firebase.analytics)
    implementation(libs.firebase.perf)
    implementation(libs.kermit)
    implementation(libs.kermit.io)

    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.robolectric.core)
    testImplementation(libs.testing.coroutines)
}
