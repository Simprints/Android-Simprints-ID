plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}
val RELEASE_CLOUD_PROJECT_ID: String by extra
val STAGING_CLOUD_PROJECT_ID: String by extra
val DEV_CLOUD_PROJECT_ID: String by extra

apply {
    from("$rootDir${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        getByName("release") {
            buildConfigField("String", "CLOUD_PROJECT_ID", "\"$RELEASE_CLOUD_PROJECT_ID\"")
        }
        getByName("staging") {
            buildConfigField("String", "CLOUD_PROJECT_ID", "\"$STAGING_CLOUD_PROJECT_ID\"")
        }
        getByName("debug") {
            buildConfigField("String", "CLOUD_PROJECT_ID", "\"$DEV_CLOUD_PROJECT_ID\"")
        }
    }
}
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":infralogging"))
    api(project(":infranetwork"))
    implementation(project(":core"))


    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    implementation(libs.firebase.auth)
    implementation(libs.kotlin.coroutinesPlayServices)
    api(libs.playServices.integrity)
    implementation(libs.retrofit.core)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines.test)
}
