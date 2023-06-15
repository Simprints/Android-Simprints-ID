plugins {
    id("simprints.infra")
    id("simprints.library.protobuf")
}

android {
    namespace = "com.simprints.infra.recent.user.activity"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}


dependencies {
    implementation(libs.datastore)
}
