plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("plugin.serialization") version Plugins.kotlinxSerializationVersion
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}
android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":moduleapi"))
    implementation(Dependencies.libsimprints)

    implementation(Dependencies.AndroidX.appcompat)
    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.AndroidX.Room.core)
    implementation(Dependencies.Kotlin.coroutines_android)
    implementation(Dependencies.Kotlin.serialization_json)
    implementation(Dependencies.Jackson.core)
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Retrofit.okhttp)
    implementation(Dependencies.Retrofit.core)
    implementation(Dependencies.SqlCipher.core)

    testImplementation(Dependencies.Testing.junit)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
}
