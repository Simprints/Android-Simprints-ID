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
            java.srcDirs("$projectDir/src/debug/java")
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

}

dependencies {
    implementation(project(":core"))
    implementation(project(":moduleapi"))
    implementation(Dependencies.libsimprints)

    implementation(Dependencies.AndroidX.Room.core)
    implementation(Dependencies.AndroidX.Room.ktx)
    kapt(Dependencies.AndroidX.Room.compiler)

    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.Kotlin.coroutines_android)
    implementation(Dependencies.Kotlin.serialization_json)
    implementation(Dependencies.Jackson.core)
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Retrofit.okhttp)
    implementation(Dependencies.Retrofit.core)
    implementation(Dependencies.SqlCipher.core)

    testImplementation(Dependencies.Testing.junit)
    testImplementation(Dependencies.Testing.AndroidX.ext_junit)
    testImplementation(Dependencies.Testing.coroutines_test)
    testImplementation(Dependencies.Testing.Robolectric.core)
    testImplementation(Dependencies.Testing.AndroidX.runner)
    testImplementation(Dependencies.Testing.AndroidX.room)
    testImplementation(Dependencies.Testing.Robolectric.multidex)
    testImplementation(project(":testtools"))
    androidTestImplementation(project(":testtools"))
    testImplementation(Dependencies.Testing.kotlin)
    testImplementation(Dependencies.Testing.truth)
    testImplementation(Dependencies.Testing.Mockk.android)
    testImplementation(Dependencies.Testing.coroutines_test)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
}
