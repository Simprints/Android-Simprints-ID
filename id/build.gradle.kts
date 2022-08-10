plugins {
    id("com.android.application")
    id("com.google.firebase.firebase-perf")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("realm-android")
    id("com.github.triplet.play")
    id("com.google.firebase.appdistribution")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
    from("${rootDir}${File.separator}buildSrc${File.separator}signing_config.gradle")
    from("${rootDir}${File.separator}ci${File.separator}deployment${File.separator}deploy_config.gradle")
}

android {
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "30L")
        }
        getByName("staging") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            withGroovyBuilder {
                "FirebasePerformance" {
                    invokeMethod("setInstrumentationEnabled", false)
                }
            }
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }

    }

    sourceSets {
        val eventCreators = "src/debug/java"
        named("test") {
            java.srcDir(eventCreators)
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }
    packagingOptions {
        // The below files are duplicated from kotlinx-coroutines-debug.
        // We should exclude them in the packaging options as per kotlinx.coroutines/kotlinx-coroutines-debug documentation
        // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-debug#build-failures-due-to-duplicate-resource-files
        resources.excludes.add("**/attach_hotspot_windows.dll")
        resources.excludes.add("META-INF/AL2.0")
        resources.excludes.add("META-INF/LGPL2.1")
        resources.excludes.add("META-INF/licenses/ASM")
    }

    buildFeatures.viewBinding = true

    dynamicFeatures.addAll(mutableSetOf(":fingerprint", ":face", ":clientapi"))
    lint {
        warning += setOf("InvalidPackage")
    }
}

repositories {
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.google.com")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    maven(url = "https://s3.amazonaws.com/repo.commonsware.com")
}

dependencies {
    // ######################################################
    //                     (Dependencies
    // ######################################################

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":core"))
    api(project(":moduleapi"))
    api(project(":eventsystem"))
    api(project(":infralogin"))
    implementation(project(":infraconfig"))
    implementation(project(":infralogging"))
    implementation(project(":infranetwork"))
    implementation(project(":infrasecurity"))
    implementation(libs.libsimprints)

    implementation(libs.dagger.core)
    implementation(libs.splitties.core)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidX.room.core)
    implementation(libs.androidX.room.ktx)
    implementation(libs.androidX.ui.cardview)
    implementation(libs.androidX.ui.preference)
    implementation(libs.androidX.ui.fragment)
    implementation(libs.androidX.security)
    implementation(libs.androidX.cameraX.camera2)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)
    implementation(libs.support.material)
    implementation(libs.workManager.work)
    implementation(libs.playServices.location)

    implementation(libs.rxJava2.core)
    kapt(libs.androidX.room.compiler)
    kapt(libs.dagger.compiler)
    implementation(libs.koin.android)
    implementation(libs.fuzzywuzzy.core)
    implementation(libs.kronos.kronos)
    implementation(libs.jackson.core)

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.barcode)
    implementation(libs.kotlin.coroutinesPlayServices)

    implementation(libs.androidX.core)
    implementation(libs.androidX.multidex)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.lifecycle.ktx)
    implementation(libs.androidX.lifecycle.livedata)
    implementation(libs.androidX.lifecycle.viewmodel)
    implementation(libs.androidX.lifecycle.scope)
    implementation(libs.androidX.ui.constraintlayout)
    api(libs.androidX.navigation.dynamicfeatures)
    implementation(libs.kotlin.coroutinesAndroid)
    implementation(libs.androidX.cameraX.core)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.playcore.core)
    implementation(libs.playcore.core.ktx)
    implementation(libs.androidX.sqlite)
    implementation(libs.sqlCipher.core)

    // ######################################################
    //                      Unit test
    // ######################################################

    testImplementation(project(":fingerprintscannermock"))
    testImplementation(project(":eventsystem"))

    testImplementation(libs.testing.retrofit)
    testImplementation(libs.testing.junit) {
        exclude("com.android.support")
    }
    testImplementation(libs.testing.robolectric.core)
    testImplementation(libs.testing.robolectric.multidex)
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.androidX.core)
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.androidX.runner)
    testImplementation(libs.testing.androidX.room)
    testImplementation(libs.testing.androidX.rules)
    testImplementation(libs.testing.espresso.core)
    testImplementation(libs.testing.espresso.intents)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.koTest.kotlin)

    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.koin)
    testImplementation(libs.testing.koin.junit4)

    testImplementation(libs.testing.mockwebserver)
    testImplementation(libs.testing.work)
    testImplementation(libs.testing.coroutines.test)
    kaptTest(libs.dagger.compiler)
    testImplementation(project(":infralogging"))
    testImplementation(project(":testtools"))

    // ######################################################
    //                      Android test
    // ######################################################

    androidTestImplementation(project(":fingerprintscannermock")) {
        exclude("org.robolectric")
    }
    androidTestImplementation(libs.testing.retrofit)
    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.core)
    androidTestUtil(libs.testing.androidX.orchestrator)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.rules)
    androidTestImplementation(libs.testing.work)
    androidTestImplementation(libs.testing.espresso.core)
    androidTestImplementation(libs.testing.espresso.intents)
    androidTestImplementation(libs.testing.truth)
    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)

    androidTestImplementation(libs.testing.mockwebserver)
    androidTestImplementation(libs.testing.coroutines.test)
    androidTestImplementation(libs.testing.androidX.room)
    androidTestImplementation(libs.rxJava2.kotlin)


    androidTestImplementation(libs.testing.espresso.barista) {
        exclude("com.android.support")
        exclude("com.google.code.findbugs")
        exclude("org.jetbrains.kotlin")
        exclude("com.google.guava")
    }
    kaptAndroidTest(libs.dagger.compiler)
    androidTestImplementation(project(":testtools")) {
        exclude("org.robolectric")
    }

    debugImplementation(libs.testing.fragment.testing) {
        exclude("androidx.test", "core")
    }
}
configurations {
    androidTestImplementation {
        // Mockk v1.1.12 and jvm 11 has the same file ValueClassSupport
        // the issue is reported here https://github.com/mockk/mockk/issues/722
        exclude("io.mockk", "mockk-agent-jvm")
        // Espresso 3.4.0 has a dependency conflict issues with "checker" and "protobuf-lite" dependancies
        // https://github.com/android/android-test/issues/861
        // and https://github.com/android/android-test/issues/999
        exclude("org.checkerframework","checker")
        exclude("com.google.protobuf", "protobuf-lite")

    }
}
kapt {
    useBuildCache = true
    arguments {
        arg("realm.ignoreKotlinNullability", true)
    }
}
