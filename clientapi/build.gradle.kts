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
        testInstrumentationRunnerArguments(mapOf(Pair("clearPackageData", "true")))
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
    implementation(Dependencies.libsimprints)

    // Service Location
    implementation(Dependencies.Koin.core)
    implementation(Dependencies.Koin.android)

    // Support
    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.AndroidX.UI.constraintlayout)
    implementation(Dependencies.AndroidX.Lifecycle.scope)

    implementation(Dependencies.Support.material)
    implementation(Dependencies.Kotlin.reflect)
    implementation(Dependencies.Timber.core)

    // Kotlin
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.AndroidX.multidex)

    // RootBeer (root detection)
    implementation(Dependencies.Rootbeer.core)

    // Unit Tests
    testImplementation(project(":testtools"))
    testImplementation(Dependencies.libsimprints)
    testImplementation(Dependencies.Testing.junit)
    testImplementation(Dependencies.Testing.AndroidX.ext_junit)
    testImplementation(Dependencies.Testing.AndroidX.core_testing)
    testImplementation(Dependencies.Testing.Robolectric.core)
    testImplementation(Dependencies.Testing.Robolectric.multidex)
    testImplementation(Dependencies.Testing.truth)
    testImplementation(Dependencies.Testing.coroutines_test)
    testImplementation(Dependencies.Testing.AndroidX.core_testing)
    testImplementation(Dependencies.Testing.AndroidX.monitor)
    testImplementation(Dependencies.Testing.AndroidX.core)
    testImplementation(Dependencies.Testing.AndroidX.ext_junit)
    testImplementation(Dependencies.Testing.Mockk.core)
    testImplementation(Dependencies.Testing.kotlin)

    testImplementation(Dependencies.Testing.Espresso.intents)
    testImplementation(Dependencies.Koin.core_ext)
    testImplementation(Dependencies.Testing.koin)

    androidTestImplementation(Dependencies.Testing.AndroidX.core_testing)
    androidTestImplementation(Dependencies.Testing.AndroidX.monitor)
    androidTestImplementation(Dependencies.Testing.AndroidX.core)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
    androidTestImplementation(Dependencies.Testing.AndroidX.runner)
    androidTestImplementation(Dependencies.Testing.Mockk.android)
    androidTestUtil(Dependencies.Testing.AndroidX.orchestrator)

    androidTestImplementation(Dependencies.Testing.AndroidX.rules)
    androidTestImplementation(Dependencies.Testing.Espresso.core)
    androidTestImplementation(Dependencies.Testing.Espresso.intents)

    androidTestImplementation(Dependencies.Koin.core_ext)
    androidTestImplementation(Dependencies.Testing.koin)
    androidTestImplementation(Dependencies.Testing.truth)

}

configurations.forEach { it.exclude("com.google.guava", "listenablefuture") }
