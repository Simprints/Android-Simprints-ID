plugins {
    id("com.android.application")
    id("com.google.firebase.firebase-perf")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("com.github.triplet.play")
    id("com.google.firebase.appdistribution")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
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
        resources.excludes.add("META-INF/LICENSE*") // remove mockk duplicated files
    }

    buildFeatures.viewBinding = true

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
    implementation(project(":core"))
    implementation(project(":moduleapi"))
    implementation(project(":eventsystem"))
    implementation(project(":infralogin"))
    implementation(project(":clientapi"))
    implementation(project(":face"))
    implementation(project(":fingerprint"))
    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infralogging"))
    implementation(project(":infranetwork"))
    implementation(project(":infrarecentuseractivity"))
    implementation(project(":infrasecurity"))
    implementation(project(":infraimages"))
    implementation(project(":infraresources"))

    implementation(libs.dagger.core)
    implementation(libs.splitties.core)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidX.ui.cardview)
    implementation(libs.androidX.ui.preference)
    implementation(libs.androidX.ui.fragment)
    runtimeOnly(libs.androidX.cameraX.camera2)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)
    implementation(libs.support.material)
    implementation(libs.workManager.work)
    implementation(libs.playServices.location)

    implementation(libs.rxJava2.core)


    // Service Location & DI
    implementation(libs.hilt)
    implementation(libs.hilt.work)
    kapt(libs.hilt.kapt)
    kapt(libs.hilt.compiler)

    implementation(libs.fuzzywuzzy.core)
    implementation(libs.jackson.core)

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.barcode)

    implementation(libs.androidX.core)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.lifecycle.livedata.ktx)
    implementation(libs.androidX.lifecycle.scope)
    implementation(libs.androidX.ui.constraintlayout)
    runtimeOnly(libs.kotlin.coroutinesAndroid)
    implementation(libs.androidX.cameraX.core)
    runtimeOnly(libs.sqlCipher.core)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    // ######################################################
    //                      Unit test
    // ######################################################
    
    testImplementation(project(":fingerprintscannermock"))
    testImplementation(project(":eventsystem"))

    testImplementation(libs.testing.hilt)
    kaptTest(libs.testing.hilt.kapt)

    testImplementation(libs.testing.retrofit)
    testImplementation(libs.testing.junit) {
        exclude("com.android.support")
    }
    testImplementation(libs.testing.robolectric.core)
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.androidX.runner)
    testImplementation(libs.testing.espresso.core)
    testImplementation(libs.testing.espresso.intents)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.koTest.kotlin.assert)

    testImplementation(libs.testing.mockk.core)

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
    androidTestImplementation(libs.testing.robolectric.core)
    androidTestImplementation(libs.testing.retrofit)
    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestUtil(libs.testing.androidX.orchestrator)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.work)
    androidTestImplementation(libs.testing.espresso.core)
    // explicitly depending on accessibility-test-framework to solve this espresso 3.4.0 build issue
    // https://github.com/android/android-test/issues/861
    androidTestImplementation(libs.testing.espresso.accessibility)
    androidTestImplementation(libs.testing.espresso.intents)
    androidTestImplementation(libs.testing.truth)
    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)

    androidTestImplementation(libs.testing.hilt)
    kaptAndroidTest(libs.testing.hilt.kapt)

    androidTestImplementation(libs.testing.mockwebserver)
    androidTestImplementation(libs.testing.coroutines.test)
    androidTestImplementation(libs.rxJava2.kotlin)
    androidTestImplementation(libs.rxJava2.android)

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

    androidTestImplementation(libs.testing.fragment.testing)
}
kapt {
    useBuildCache = true
    arguments {
        arg("realm.ignoreKotlinNullability", true)
    }
}
