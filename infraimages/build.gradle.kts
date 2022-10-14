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

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":infralogging"))
    implementation(project(":infralogin"))
    implementation(project(":core"))
    implementation(project(":infrasecurity"))
    implementation(project(":infraconfig"))

    implementation(libs.androidX.core)

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)

    implementation(libs.androidX.security)
    implementation(libs.kotlin.coroutinesAndroid)
    implementation(libs.kotlin.coroutinesPlayServices)

    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.core.testing)
    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines.test)

    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.core)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.androidX.rules)
    androidTestImplementation(libs.testing.mockk.core)
    androidTestImplementation(libs.testing.mockk.android)
    androidTestImplementation(libs.testing.truth)
}
