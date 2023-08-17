plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.simafiswrapper"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    runtimeOnly(libs.simmatcher)
}
