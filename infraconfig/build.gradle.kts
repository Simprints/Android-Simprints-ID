import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("realm-android")
    id("com.google.protobuf") version "0.8.17"
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
    implementation(project(":infranetwork"))
    implementation(project(":infralogging"))
    implementation(project(":infralogin"))
    implementation(project(":infrasecurity"))
    implementation(project(":infrarealm"))

    implementation(libs.androidX.core)
    implementation(libs.datastore)
    implementation(libs.protobuf)

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    implementation(libs.jackson.core)

    implementation(libs.retrofit.core)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines.test)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.4"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

kapt {
    correctErrorTypes = true
}

