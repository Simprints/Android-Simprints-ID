plugins {
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("realm-android")
    id("com.github.triplet.play")
    id("com.google.firebase.appdistribution")
    id("com.google.gms.google-services")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
    from("${rootDir}${File.separator}buildSrc${File.separator}signing_config.gradle")
    from("${rootDir}${File.separator}ci${File.separator}deployment${File.separator}deploy_config.gradle")
}

val RELEASE_SAFETYNET_KEY: String by extra
val STAGING_SAFETYNET_KEY: String by extra
val DEV_SAFETYNET_KEY: String by extra

android {

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments(mapOf(Pair("clearPackageData", "true")))

        ndk.abiFilters("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

        javaCompileOptions {
            annotationProcessorOptions {
                //Required by Room to be able to export the db schemas
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    lintOptions.warning("InvalidPackage")

    bundle {
        language {
            enableSplit = false
        }
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(true)
            extra.set("enableCrashlytics", true)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            manifestPlaceholders["firebase_performance_logcat_enabled"] = false
            manifestPlaceholders["firebase_analytics_collection_enabled"] = true
            buildConfigField("String", "SAFETYNET_API_KEY", "\"$RELEASE_SAFETYNET_KEY\"")
            buildConfigField(
                "String",
                "LONG_CONSENT_BUCKET",
                "\"gs://simprints-152315-firebase-storage\""
            )
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "30L")
        }
        getByName("staging") {
            minifyEnabled(true)
            extra.set("enableCrashlytics", true)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            manifestPlaceholders["firebase_performance_logcat_enabled"] = false
            manifestPlaceholders["firebase_analytics_collection_enabled"] = true
            buildConfigField("String", "SAFETYNET_API_KEY", "\"$STAGING_SAFETYNET_KEY\"")
            buildConfigField(
                "String",
                "LONG_CONSENT_BUCKET",
                "\"gs://simprints-firebase-staging.appspot.com\""
            )
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            minifyEnabled(false)
            extra.set("enableCrashlytics", false)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            manifestPlaceholders["firebase_performance_logcat_enabled"] = false
            manifestPlaceholders["firebase_analytics_collection_enabled"] = true
            withGroovyBuilder {
                "FirebasePerformance" {
                    invokeMethod("setInstrumentationEnabled", false)
                }
            }
            buildConfigField("String", "SAFETYNET_API_KEY", "\"$DEV_SAFETYNET_KEY\"")
            buildConfigField(
                "String",
                "LONG_CONSENT_BUCKET",
                "\"gs://simprints-dev-firebase-storage\""
            )
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }

    }

    sourceSets {
        val sharedTestDir = "src/commontesttools/java"
        val eventCreators = "src/debug/java"
        named("test") {
            java.srcDir(sharedTestDir)
            java.srcDir(eventCreators)
        }
        named("androidTest") {
            java.srcDir(sharedTestDir)
            java.srcDir("src/androidTest/java")
        }

        // Adds exported room schema location as test app assets.
        named("debug") {
            assets.srcDirs("$projectDir/schemas")
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        animationsDisabled = true
    }

    buildFeatures.viewBinding = true

    dynamicFeatures = mutableSetOf(":fingerprint", ":face", ":clientapi")
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
    implementation(Dependencies.libsimprints)

    implementation(Dependencies.Dagger.core)
    implementation(Dependencies.Kotlin.reflect)
    api(Dependencies.Kotlin.anko)
    implementation(Dependencies.AndroidX.Room.core)
    implementation(Dependencies.AndroidX.Room.ktx)
    implementation(Dependencies.AndroidX.UI.cardview)
    implementation(Dependencies.AndroidX.UI.preference)
    implementation(Dependencies.AndroidX.UI.fragment)
    implementation(Dependencies.AndroidX.security)
    implementation(Dependencies.AndroidX.CameraX.camera2)
    implementation(Dependencies.AndroidX.CameraX.lifecycle)
    implementation(Dependencies.AndroidX.CameraX.view)
    implementation(Dependencies.Support.material)
    implementation(Dependencies.WorkManager.work)
    implementation(Dependencies.PlayServices.location)
    implementation(Dependencies.PlayServices.places)
    implementation(Dependencies.PlayServices.safetynet)
    implementation(Dependencies.Retrofit.core)
    implementation(Dependencies.Retrofit.adapter)
    implementation(Dependencies.Retrofit.jackson)
    implementation(Dependencies.Retrofit.logging)
    implementation(Dependencies.Retrofit.okhttp)
    implementation(Dependencies.Retrofit.converterScalars)
    implementation(Dependencies.RxJava2.core)
    kapt(Dependencies.AndroidX.Room.compiler)
    kapt(Dependencies.Dagger.compiler)
    implementation(Dependencies.Koin.android)
    implementation(Dependencies.Fuzzywuzzy.core)
    implementation(Dependencies.Kronos.kronos)
    implementation(Dependencies.Jackson.core)

    // RootBeer (root detection)
    implementation(Dependencies.Rootbeer.core)

    // Firebase
    api (Dependencies.Firebase.analytics)
    implementation(Dependencies.Firebase.auth)
    implementation(Dependencies.Firebase.config)
    implementation(Dependencies.Firebase.perf)
    implementation(Dependencies.Firebase.storage)
    implementation(Dependencies.Firebase.mlkit)
    implementation(Dependencies.Firebase.mlkit_barcode)
    implementation(Dependencies.Firebase.crashlytics)

    implementation(Dependencies.Timber.core)
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.AndroidX.multidex)
    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.AndroidX.Lifecycle.ktx)
    implementation(Dependencies.AndroidX.Lifecycle.livedata)
    implementation(Dependencies.AndroidX.Lifecycle.viewmodel)
    implementation(Dependencies.AndroidX.Lifecycle.scope)
    implementation(Dependencies.AndroidX.UI.constraintlayout)
    api(Dependencies.AndroidX.Navigation.dynamicfeatures)
    implementation(Dependencies.Kotlin.coroutines_android)
    implementation(Dependencies.Kotlin.coroutines_play_services)
    implementation(Dependencies.AndroidX.CameraX.core)
    implementation(Dependencies.Koin.core)
    implementation(Dependencies.Koin.android)
    api(Dependencies.Koin.viewmodel)
    implementation(Dependencies.Playcore.core)
    implementation(Dependencies.Playcore.core_ktx)
    implementation(Dependencies.AndroidX.sqlite)
    implementation(Dependencies.SqlCipher.core)

    // ######################################################
    //                      Unit test
    // ######################################################

    testImplementation(project(":fingerprintscannermock"))
    testImplementation(Dependencies.Testing.retrofit)
    testImplementation(Dependencies.Testing.junit) {
        exclude("com.android.support")
    }
    testImplementation(Dependencies.Testing.Robolectric.core)
    testImplementation(Dependencies.Testing.Robolectric.multidex)
    testImplementation(Dependencies.Testing.AndroidX.ext_junit)
    testImplementation(Dependencies.Testing.AndroidX.core)
    testImplementation(Dependencies.Testing.AndroidX.core_testing)
    testImplementation(Dependencies.Testing.AndroidX.runner)
    testImplementation(Dependencies.Testing.AndroidX.room)
    testImplementation(Dependencies.Testing.AndroidX.rules)
    testImplementation(Dependencies.Testing.Espresso.core)
    testImplementation(Dependencies.Testing.Espresso.intents)
    testImplementation(Dependencies.Testing.Espresso.contrib)
    testImplementation(Dependencies.Testing.truth)
    testImplementation(Dependencies.Testing.kotlin)

    testImplementation(Dependencies.Testing.Mockk.core)
    testImplementation(Dependencies.Testing.koin)

    testImplementation(Dependencies.Testing.mockwebserver)
    testImplementation(Dependencies.Testing.work)
    testImplementation(Dependencies.Testing.coroutines_test)
    kaptTest(Dependencies.Dagger.compiler)
    testImplementation(project(":testtools")) {
        exclude("org.mockito:mockito-android")
    }

    // ######################################################
    //                      Android test
    // ######################################################

    androidTestImplementation(project(":fingerprintscannermock")) {
        exclude("org.apache.maven")
        exclude("org.mockito")
        exclude("org.robolectric")
    }
    androidTestImplementation(Dependencies.Testing.Mockito.core)
    androidTestImplementation(Dependencies.Testing.Mockito.android)
    androidTestImplementation(Dependencies.Testing.Mockito.kotlin)
    androidTestImplementation(Dependencies.Testing.retrofit)
    androidTestImplementation(Dependencies.Testing.AndroidX.core_testing)
    androidTestImplementation(Dependencies.Testing.AndroidX.monitor)
    androidTestImplementation(Dependencies.Testing.AndroidX.core)
    androidTestUtil(Dependencies.Testing.AndroidX.orchestrator)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
    androidTestImplementation(Dependencies.Testing.AndroidX.runner)
    androidTestImplementation(Dependencies.Testing.AndroidX.rules)
    androidTestImplementation(Dependencies.Testing.work)
    androidTestImplementation(Dependencies.Testing.Espresso.core)
    androidTestImplementation(Dependencies.Testing.Espresso.intents)
    androidTestImplementation(Dependencies.Testing.truth)
    androidTestImplementation(Dependencies.Testing.rx2_idler)
    androidTestImplementation(Dependencies.Testing.Mockk.core)
    androidTestImplementation(Dependencies.Testing.Mockk.android)
    androidTestImplementation(Dependencies.Testing.mockwebserver)
    androidTestImplementation(Dependencies.Testing.coroutines_test)
    androidTestImplementation(Dependencies.Testing.AndroidX.room)
    androidTestImplementation(Dependencies.RxJava2.kotlin)

    androidTestImplementation(Dependencies.Testing.awaitility) {
        exclude("org.hamcrest")
    }

    androidTestImplementation(Dependencies.Testing.Espresso.barista) {
        exclude("com.android.support")
        exclude("com.google.code.findbugs")
        exclude("org.jetbrains.kotlin")
        exclude("com.google.guava")
    }
    kaptAndroidTest(Dependencies.Dagger.compiler)
    androidTestImplementation(project(":testtools")) {
        exclude("org.apache.maven")
        exclude("org.mockito")
        exclude("org.robolectric")
        exclude("org.jetbrains.kotlinx")
    }
    androidTestImplementation(Dependencies.Testing.kappuccino)

}

kapt {
    useBuildCache = true
    arguments {
        arg("realm.ignoreKotlinNullability", true)
    }
}
