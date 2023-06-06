plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.matcher"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    runtimeOnly(libs.simmatcher)
}
