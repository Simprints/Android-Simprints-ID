plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.scanner"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "1440L")
        }

        getByName("staging") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }

        getByName("debug") {
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    implementation(libs.workManager.work)
    implementation(libs.retrofit.core)
    runtimeOnly(libs.jackson.core)

    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:recent-user-activity"))

    // RxJava
    api(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)

    // If mock/dummy BT adapter is required test implementation can be switched to regular one
    testImplementation(project(":fingerprint:infra:scannermock"))
    //implementation(project(":fingerprint:infra:scannermock"))

}
