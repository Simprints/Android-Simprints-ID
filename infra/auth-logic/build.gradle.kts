plugins {
    id("simprints.infra")
    id("simprints.config.cloud")
}

android {
    namespace = "com.simprints.infra.authlogic"


    buildTypes {
        getByName("release") {
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "30L")
        }
        getByName("staging") {
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:project-security-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:events"))

    implementation(project(":infra:event-sync"))
    implementation(project(":infra:enrolment-records-sync"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:images"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":fingerprint:infra:scanner"))

    implementation(libs.retrofit.core)
    implementation(libs.playServices.integrity)
    implementation(libs.workManager.work)
}
