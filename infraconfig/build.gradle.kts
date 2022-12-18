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
    implementation(project(":infranetwork"))
    implementation(project(":infralogging"))
    api(project(":infralogin"))
    api(project(":infrarealm"))

    implementation(libs.workManager.work)

    implementation(libs.datastore)
    api(libs.protobuf)
    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)


    implementation(libs.hilt)
    api(libs.hilt.work)
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

