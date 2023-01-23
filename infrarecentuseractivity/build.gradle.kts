plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("com.google.protobuf") version "0.9.1"
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    namespace = "com.simprints.infra.recent.user.activity"
}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":infralogging"))

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    implementation(libs.datastore)
    api(libs.protobuf)

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
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
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
