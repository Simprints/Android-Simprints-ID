plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.sync"

    buildTypes {
        getByName("release") {
            buildConfigField("long", "PROJECT_DOWN_SYNC_WORKER_INTERVAL_MINUTES", "60L")
            buildConfigField("long", "DEVICE_DOWN_SYNC_WORKER_INTERVAL_MINUTES", "30L")
            buildConfigField("long", "FILE_UP_SYNC_WORKER_INTERVAL_MINUTES", "60L")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "1440L")
            buildConfigField("long", "EVENT_SYNC_WORKER_INTERVAL_MINUTES", "60L")
        }
        getByName("staging") {
            buildConfigField("long", "PROJECT_DOWN_SYNC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "DEVICE_DOWN_SYNC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "FILE_UP_SYNC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "EVENT_SYNC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            buildConfigField("long", "PROJECT_DOWN_SYNC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "DEVICE_DOWN_SYNC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "FILE_UP_SYNC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "EVENT_SYNC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:images"))

    implementation(project(":fingerprint:infra:scanner"))
    implementation(project(":fingerprint:infra:image-distortion-config"))

    implementation(libs.workManager.work)
}
