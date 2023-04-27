plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprintmatcher"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    runtimeOnly(libs.simmatcher)
}
