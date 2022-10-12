plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("kapt")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests {
            all {
                it.testLogging {
                    events("started", "passed", "skipped", "failed")
                }
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.androidX.annotation.annotation)
    implementation(libs.kotlin.coroutine.rx2.adapter)
    implementation(project(":infralogging"))

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // RxJava
    api(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)

    // ######################################################
    //                      Unit test
    // ######################################################

    // Simprints
    testImplementation(project(":testtools"))

    // JUnit
    testImplementation(libs.testing.junit)

    // Mockito
    testImplementation(libs.testing.mockito.kotlin)
    testImplementation(libs.testing.mockito.core)
    testImplementation(libs.testing.mockito.inline)

    // Truth
    testImplementation(libs.testing.truth)
}
