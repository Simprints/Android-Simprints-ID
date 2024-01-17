plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
    id("simprints.testing.android")
}

android {
    namespace = "com.simprints.infra.images"

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
    implementation(project(":infra:config-store"))

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)

    implementation(libs.androidX.security)
    implementation(libs.kotlin.coroutinesPlayServices)
    implementation(libs.workManager.work)

}
