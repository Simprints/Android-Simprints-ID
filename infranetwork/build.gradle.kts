plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            buildConfigField("String", "BASE_URL_PREFIX", "\"prod\"")
        }
        getByName("staging") {
            buildConfigField("String", "BASE_URL_PREFIX", "\"staging\"")
        }
        getByName("debug") {
            buildConfigField("String", "BASE_URL_PREFIX", "\"dev\"")
        }
    }
}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":infralogging"))

    debugImplementation(libs.chuck.debug)
    implementation(libs.chuck.release)

    implementation(libs.androidX.core)

    api(libs.jackson.core)

    implementation(libs.kotlin.coroutinesAndroid)

    implementation(libs.retrofit.adapter)
    implementation(libs.retrofit.converterScalars)
    api(libs.retrofit.core)
    api(libs.retrofit.jackson)
    api(libs.retrofit.logging)
    api(libs.retrofit.okhttp)

    // This implementation is to fix the issue with duplicated class on the view model
    implementation(libs.androidX.lifecycle.viewmodel)
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.chuck.release)

    testImplementation(libs.testing.coroutines.test)

    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.mockwebserver)
    testImplementation(libs.testing.truth)
}

configurations {
    debugImplementation {
        // We have two versions of chucker, a dummy one "library-no-op" that is designed for release and staging build types
        // And a full feature version that should be added in debug build types
        exclude("com.github.chuckerteam.chucker", "library-no-op")
    }
}
