plugins {
    id("com.android.library")
    id("kotlin-android")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")

        javaCompileOptions {
            annotationProcessorOptions {
                //Required by Room to be able to export the db schemas
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }

    sourceSets {
        // Adds exported room schema location as test app assets.
        getByName("debug") {
            assets.srcDirs("$projectDir/schemas")
        }
        getByName("test") {
            java.srcDirs("$projectDir/src/debug")
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
    namespace = "com.simprints.infra.events"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":infraconfig"))
    implementation(project(":infralogin"))

    implementation(libs.androidX.room.ktx)
    kapt(libs.androidX.room.compiler)

    runtimeOnly(libs.kotlin.coroutinesAndroid)
    implementation(libs.sqlCipher.core)

    implementation(libs.jackson.core)

    implementation(libs.workManager.work)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.coroutines)
    testImplementation(libs.testing.robolectric.annotation)
    testImplementation(libs.testing.koTest.kotlin.assert)
    testImplementation(libs.testing.androidX.room)
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.hilt)

    androidTestImplementation(libs.testing.androidX.core)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.mockk.android)

    androidTestImplementation(project(":infraenrolmentrecords"))
}

