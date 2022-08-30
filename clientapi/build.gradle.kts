plugins {
    id("com.android.dynamic-feature")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
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
    implementation(project(":moduleapi"))
    implementation(project(":infralogging"))
    implementation(project(":infrasecurity"))
    implementation(project(":infrarealm"))
    implementation(project(":infranetwork"))
    implementation(libs.libsimprints)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Support
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.security)
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.lifecycle.scope)
    implementation(libs.support.material)

    // Splitties
    implementation(libs.splitties.core)
    // Kotlin
    implementation(libs.androidX.core)
    implementation(libs.androidX.multidex)

    // Unit Tests
    testImplementation(project(":testtools"))
    testImplementation(libs.libsimprints)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.coroutines.test)
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.androidX.core)
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.koTest.kotlin)


    testImplementation(libs.testing.espresso.intents)

    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.core)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.runner)
    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)
    androidTestImplementation(libs.testing.androidX.orchestrator)

    androidTestImplementation(libs.testing.androidX.rules)
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)

    androidTestImplementation(libs.testing.truth)
}

configurations {
    androidTestImplementation {
        // Mockk v1.1.12 and jvm 11 has the same file ValueClassSupport
        // the issue is reported here https://github.com/mockk/mockk/issues/722
        exclude("io.mockk", "mockk-agent-jvm")
    }
    forEach {
        it.exclude("com.google.guava", "listenablefuture")
    }
}
