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

}

dependencies {
    // We specifically don't include Crashlytics, Analytics and Timber in the central buildSrc
    // module because we do not want or expect these dependencies to be used in multiple modules
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.2.1")
    implementation("com.google.firebase:firebase-analytics-ktx:19.0.1")
    implementation("com.google.firebase:firebase-perf:19.0.5")

    //4.7.1 breaks realm:
    // https://github.com/realm/realm-java/issues/6153
    // https://github.com/JakeWharton/timber/issues/295
    implementation("com.jakewharton.timber:timber:5.0.1") {
        exclude("org.jetbrains", "annotations")
    }

    // Unit Tests
    testImplementation(Dependencies.Testing.junit)
}
