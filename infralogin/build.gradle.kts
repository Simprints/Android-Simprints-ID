plugins {
    id("simprints.infra")
    id("simprints.library.room")
    id("simprints.config.cloud")
}

android {
    namespace = "com.simprints.infra.login"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {

    implementation(libs.firebase.auth)
    implementation(libs.kotlin.coroutinesPlayServices)
    implementation(libs.playServices.integrity)
    implementation(libs.retrofit.core)
}
