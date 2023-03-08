plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    namespace = "com.simprints.infra.eventsync"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(project(":infralogging"))
    api(project(":infraevents"))
    implementation(project(":infranetwork"))
    implementation(project(":infraenrolmentrecords"))

    implementation(libs.androidX.room.ktx)
    kapt(libs.androidX.room.compiler)

    runtimeOnly(libs.kotlin.coroutinesAndroid)
    api(libs.sqlCipher.core)
    implementation(libs.workManager.work)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    implementation(libs.workManager.work)

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
    testImplementation(libs.hilt.testing)

    androidTestImplementation(libs.testing.androidX.core.testing)
    androidTestImplementation(libs.testing.androidX.ext.junit)
    androidTestImplementation(libs.testing.mockk.android)

}
