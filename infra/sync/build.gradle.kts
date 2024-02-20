plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.sync"


    buildTypes {
        getByName("release") {
            buildConfigField("long", "PROJECT_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
            buildConfigField("long", "DEVICE_PERIODIC_WORKER_INTERVAL_MINUTES", "30L")
            buildConfigField("long", "IMAGE_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "1440L")
            buildConfigField("long", "EVENT_SYNC_WORKER_INTERVAL_MINUTES", "60L")
        }
        getByName("staging") {
            buildConfigField("long", "PROJECT_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "DEVICE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "IMAGE_PERIODIC_WORKER_INTERVAL_MINUTES", "15LL")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15LL")
            buildConfigField("long", "EVENT_SYNC_WORKER_INTERVAL_MINUTES", "15LL")
        }
        getByName("debug") {
            buildConfigField("long", "PROJECT_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "DEVICE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "IMAGE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "FIRMWARE_UPDATE_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "EVENT_SYNC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:images"))

    implementation(project(":fingerprint:infra:scanner"))

    implementation(libs.workManager.work)
}
