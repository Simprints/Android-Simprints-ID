plugins {
    id("simprints.infra")
    id("simprints.library.protobuf")
}

android {
    namespace = "com.simprints.infra.config"

    defaultConfig {
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
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:realm"))

    implementation(libs.workManager.work)

    implementation(libs.datastore)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
