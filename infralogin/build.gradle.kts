plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

apply {
    from("$rootDir${File.separator}buildSrc${File.separator}build_config.gradle")
}

val RELEASE_SAFETYNET_KEY: String by extra
val STAGING_SAFETYNET_KEY: String by extra
val DEV_SAFETYNET_KEY: String by extra

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            buildConfigField("String", "SAFETYNET_API_KEY", "\"$RELEASE_SAFETYNET_KEY\"")
        }
        getByName("staging") {
            buildConfigField("String", "SAFETYNET_API_KEY", "\"$STAGING_SAFETYNET_KEY\"")
        }
        getByName("debug") {
            buildConfigField("String", "SAFETYNET_API_KEY", "\"$DEV_SAFETYNET_KEY\"")
        }

    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":infralogging"))
    implementation(project(":infranetwork"))
    implementation(project(":core"))

    implementation(libs.androidX.core)

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    implementation(libs.firebase.auth)
    implementation(libs.kotlin.coroutinesPlayServices)
    api(libs.playServices.safetynet)

    implementation(libs.retrofit.core)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines.test)
}
