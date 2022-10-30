import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("realm-android")
    id("com.google.protobuf") version "0.8.19"
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
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
        }
        getByName("staging") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }

    }
}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    api(project(":infranetwork"))
    implementation(project(":infralogging"))
    implementation(project(":infralogin"))
    implementation(project(":infrasecurity"))
    implementation(project(":infrarealm"))

    implementation(libs.androidX.core)
    implementation(libs.workManager.work)

    implementation(libs.datastore)
    implementation(libs.protobuf)

    implementation(libs.hilt)
    implementation(libs.hilt.work)
    kapt(libs.hilt.kapt)
    kapt(libs.hilt.compiler)

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
        artifact = "com.google.protobuf:protoc:3.21.9"
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

