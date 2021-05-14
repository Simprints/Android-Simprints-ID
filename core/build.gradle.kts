plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    buildTypes {
        getByName("release") {
            buildConfigField("String", "BASE_URL_PREFIX", "\"prod\"")
        }
        getByName("staging") {
            buildConfigField("String", "BASE_URL_PREFIX", "\"staging\"")
        }
        getByName("debug") {
            buildConfigField("String", "BASE_URL_PREFIX", "\"dev\"")
        }
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.AndroidX.Lifecycle.java8)

    implementation(Dependencies.Kotlin.coroutines_android)
    api(Dependencies.Testing.Espresso.idling)
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Jackson.core)
    api(Dependencies.Dagger.javax)
    implementation(Dependencies.Firebase.storage)

    implementation(Dependencies.Playcore.core)

    testImplementation(Dependencies.Testing.truth)
    testImplementation(Dependencies.Testing.junit)
    testImplementation(Dependencies.Testing.Mockk.core)
    testImplementation(Dependencies.Testing.coroutines_test)
    testImplementation(Dependencies.Testing.kotlin)
    testImplementation(project(":testtools"))

    androidTestImplementation(Dependencies.Testing.AndroidX.core_testing)
    androidTestImplementation(Dependencies.Testing.AndroidX.monitor)
    androidTestImplementation(Dependencies.Testing.AndroidX.core)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
    androidTestImplementation(Dependencies.Testing.AndroidX.runner)
    androidTestImplementation(Dependencies.Testing.AndroidX.rules)
    androidTestImplementation(Dependencies.Testing.truth)
    androidTestUtil(Dependencies.Testing.AndroidX.orchestrator)
    androidTestImplementation(Dependencies.Testing.live_data)
}
