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


    sourceSets {
        val sharedTestDir = "src/commontesttools/java"
        named("test") {
            java.srcDir(sharedTestDir)
        }
        named("androidTest") {
            java.srcDir(sharedTestDir)
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
    implementation(project(":logging"))

    // Kotlin
    implementation(Dependencies.Kotlin.reflect)

    // Android X
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.AndroidX.multidex)
    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.AndroidX.legacy)
    implementation(Dependencies.AndroidX.Room.core)
    implementation(Dependencies.AndroidX.Lifecycle.viewmodel)
    implementation(Dependencies.AndroidX.Lifecycle.livedata)
    implementation(Dependencies.AndroidX.UI.constraintlayout)
    implementation(Dependencies.AndroidX.UI.cardview)
    implementation(Dependencies.AndroidX.UI.preference)
    implementation(Dependencies.AndroidX.UI.fragment)
    implementation(Dependencies.AndroidX.UI.viewpager2)
    implementation(Dependencies.AndroidX.Navigation.fragment)
    implementation(Dependencies.AndroidX.Navigation.ui)
    implementation(Dependencies.WorkManager.work)

    // RxJava
    implementation(Dependencies.RxJava2.permissions)
    implementation(Dependencies.RxJava2.location)
    implementation(Dependencies.RxJava2.android)
    implementation(Dependencies.RxJava2.core)
    implementation(Dependencies.RxJava2.kotlin)

    // Play Services
    implementation(Dependencies.PlayServices.location)

    // Retrofit
    implementation(Dependencies.Retrofit.core)
    implementation(Dependencies.Retrofit.adapter)
    implementation(Dependencies.Retrofit.logging)
    implementation(Dependencies.Retrofit.okhttp)

    // ######################################################
    //                      Unit test
    // ######################################################

    // Simprints
    testImplementation(project(":testtools"))
    testImplementation(Dependencies.Testing.junit) {
        exclude("com.android.support")
    }

    // Android X
    testImplementation(Dependencies.Testing.AndroidX.ext_junit)
    testImplementation(Dependencies.Testing.AndroidX.core)
    testImplementation(Dependencies.Testing.AndroidX.core_testing)
    testImplementation(Dependencies.Testing.AndroidX.runner)
    testImplementation(Dependencies.Testing.AndroidX.room)
    testImplementation(Dependencies.Testing.AndroidX.rules)

    // Espresso
    testImplementation(Dependencies.Testing.Espresso.core)
    testImplementation(Dependencies.Testing.Espresso.intents)
    testImplementation(Dependencies.Testing.Espresso.contrib)

    // Kotlin
    testImplementation(Dependencies.Testing.KoTest.kotlin)
    testImplementation(Dependencies.Testing.coroutines_test)

    // Mocking and assertion frameworks
    testImplementation(Dependencies.Testing.Mockito.kotlin)
    testImplementation(Dependencies.Testing.truth)
    testImplementation(Dependencies.Testing.Mockk.core)

    // Koin
    testImplementation(Dependencies.Testing.koin)

    // Robolectric
    testImplementation(Dependencies.Testing.Robolectric.core)
    testImplementation(Dependencies.Testing.Robolectric.multidex)

    // ######################################################
    //                      Android test
    // ######################################################

    // Simprints
    androidTestImplementation(project(":testtools")) {
        exclude("org.apache.maven")
        exclude("org.mockito")
        exclude("org.robolectric")
        exclude("org.jetbrains.kotlinx")
        exclude("io.mockk")
    }

    // Koin
    androidTestImplementation(Dependencies.Testing.koin)

    // Android X
    androidTestImplementation(Dependencies.Testing.AndroidX.core_testing)
    androidTestImplementation(Dependencies.Testing.AndroidX.monitor)
    androidTestImplementation(Dependencies.Testing.AndroidX.core)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
    androidTestImplementation(Dependencies.Testing.AndroidX.runner)
    androidTestImplementation(Dependencies.Testing.AndroidX.rules)
    androidTestUtil(Dependencies.Testing.AndroidX.orchestrator)
    androidTestImplementation(Dependencies.Testing.live_data)
    androidTestImplementation(Dependencies.AndroidX.CameraX.core)

    // Android X navigation components
    androidTestImplementation(Dependencies.Testing.AndroidX.navigation)

    // Mocking and assertion frameworks
    androidTestImplementation(Dependencies.Testing.Mockito.core)
    androidTestImplementation(Dependencies.Testing.Mockito.android)
    androidTestImplementation(Dependencies.Testing.Mockito.kotlin)
    androidTestImplementation(Dependencies.Testing.truth)
    androidTestImplementation(Dependencies.Testing.Mockk.core)
    androidTestImplementation(Dependencies.Testing.Mockk.android)

    // Espresso
    androidTestImplementation(Dependencies.Testing.Espresso.core)
    androidTestImplementation(Dependencies.Testing.Espresso.intents)
    androidTestImplementation(Dependencies.Testing.Espresso.barista) {
        exclude("com.android.support")
        exclude("com.google.code.findbugs")
        exclude("org.jetbrains.kotlin")
        exclude("com.google.guava")
    }

    // Truth
    androidTestImplementation(Dependencies.Testing.truth)

    androidTestImplementation(Dependencies.Testing.rx2_idler)

    // Trust me I hate this fix more than you
    // This is to solve multiple imports of guava https://stackoverflow.com/a/60492942/4072335
    androidTestImplementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
}

configurations {
    androidTestImplementation {
        // Mockk v1.1.12 and jvm 11 has the same file ValueClassSupport
        // the issue is reported here https://github.com/mockk/mockk/issues/722
        exclude("io.mockk", "mockk-agent-jvm")
    }
}
