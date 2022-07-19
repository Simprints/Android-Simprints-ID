plugins {
    id("com.android.library")
    kotlin("android")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":infralogging"))

    debugImplementation(libs.chuck.debug)
    releaseImplementation(libs.chuck.release)

    implementation(libs.androidX.core)

    implementation(libs.jackson.core)

    implementation(libs.kotlin.coroutinesAndroid)

    implementation(libs.retrofit.adapter)
    implementation(libs.retrofit.converterScalars)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.jackson)
    implementation(libs.retrofit.logging)
    implementation(libs.retrofit.okhttp)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.chuck.release)

    testImplementation(libs.testing.coroutines.test)

    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.mockwebserver)
    testImplementation(libs.testing.truth)
}
