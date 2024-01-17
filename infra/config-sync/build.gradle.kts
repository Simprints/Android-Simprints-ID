plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.config.sync"


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
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:images"))

    implementation(libs.workManager.work)
}
