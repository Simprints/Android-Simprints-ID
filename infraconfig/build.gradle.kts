plugins {
    id("simprints.infra")
    id("simprints.library.protobuf")
}

android {
    namespace = "com.simprints.infra.config"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures.buildConfig = true
    buildTypes {
        getByName("release") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
        }
        create("staging") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {
    implementation(project(":infralogin"))
    implementation(project(":infrarealm"))

    implementation(libs.workManager.work)

    implementation(libs.datastore)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
