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
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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
    testImplementation(Dependencies.Testing.truth)
    testImplementation(Dependencies.Testing.Mockk.android)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
}
