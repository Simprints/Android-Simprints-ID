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
    packagingOptions {
        resources.excludes.add("META-INF/LICENSE*") // remove mockk duplicated files
    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    buildFeatures.viewBinding = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    api(project(":moduleapi"))
    api(project(":infraconfig"))
    api(project(":infraenrolmentrecords"))
    implementation(project(":infralogging"))
    api(project(":infrasecurity"))
    api(project(":core"))
    api(project(":eventsystem"))
    implementation(project(":infraresources"))

    api(libs.libsimprints)

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
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.coroutines.test)
    testImplementation(libs.testing.androidX.core)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.koTest.kotlin.assert)

    testImplementation(libs.testing.espresso.intents)

    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.runner)
    androidTestImplementation(libs.testing.mockk.android)

    androidTestImplementation(libs.testing.androidX.rules)
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)

    androidTestImplementation(libs.testing.truth)
}
