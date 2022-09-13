plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

apply {
    from("$rootDir${File.separator}buildSrc${File.separator}build_config.gradle")
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
    implementation(project(":infralogin"))
    implementation(project(":core"))

    implementation(libs.androidX.core)

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Firebase
    implementation(libs.firebase.storage)

    implementation(libs.androidX.security)
    implementation(libs.kotlin.coroutinesAndroid)
    implementation(libs.kotlin.coroutinesPlayServices)
}
