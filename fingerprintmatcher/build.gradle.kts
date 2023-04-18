plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprintmatcher"

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

dependencies {
    runtimeOnly(libs.simmatcher)
}
