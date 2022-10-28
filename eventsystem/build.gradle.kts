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

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    api(project(":core"))
    implementation(project(":moduleapi"))
    implementation(project(":infraconfig"))
    implementation(project(":infralogging"))
    implementation(project(":infralogin"))
    api(project(":infranetwork"))
    implementation(project(":infrasecurity"))

    implementation(libs.androidX.room.ktx)
    kapt(libs.androidX.room.compiler)

    implementation(libs.kotlin.coroutinesAndroid)

    implementation(libs.sqlCipher.core)

    // DI
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.coroutines.test)
    testImplementation(libs.testing.robolectric.annotation)
    testImplementation(libs.testing.koTest.kotlin.assert)
    testImplementation(libs.testing.androidX.room)
    testImplementation(project(":testtools"))
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)

    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.rules)
    androidTestImplementation(libs.testing.mockk.android)

    androidTestImplementation(project(":infraenrolmentrecords"))
}

