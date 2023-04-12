plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
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
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    buildFeatures.viewBinding = true
    namespace = "com.simprints.clientapi"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":infraresources"))
    implementation(project(":featurealert"))
    implementation(project(":core"))
    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraeventsync"))
    implementation(project(":infraevents"))
    implementation(project(":featurealert"))

    implementation(libs.libsimprints)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Support
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.lifecycle.scope)
    implementation(libs.support.material)

    // Kotlin
    implementation(libs.androidX.core)

    // Unit Tests
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.androidX.core)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.coroutines)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.koTest.kotlin.assert)

    testImplementation(libs.testing.espresso.intents)

    androidTestImplementation(libs.testing.androidX.core)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.runner)
    androidTestImplementation(libs.testing.mockk.android)

    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)

    androidTestImplementation(libs.testing.truth)
}
