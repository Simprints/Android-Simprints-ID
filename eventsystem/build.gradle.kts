plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("kapt")
    kotlin("plugin.serialization") version Plugins.kotlinxSerializationVersion
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

}

dependencies {
    api(project(":core"))
    api(project(":moduleapi"))
    implementation(project(":infralogging"))
    implementation(project(":infralogin"))
    api(project(":infranetwork"))
    implementation(project(":infrasecurity"))

    api(libs.androidX.room.core)
    implementation(libs.androidX.room.ktx)
    kapt(libs.androidX.room.compiler)

    implementation(libs.androidX.core)
    implementation(libs.kotlin.coroutinesAndroid)
    implementation(libs.jackson.core)

    api(libs.retrofit.okhttp)
    api(libs.retrofit.core)
    api(libs.sqlCipher.core)

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.coroutines.test)
    testImplementation(libs.testing.robolectric.annotation)
    testImplementation(libs.testing.koTest.kotlin.assert)
    testImplementation(libs.testing.androidX.room)
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
}
