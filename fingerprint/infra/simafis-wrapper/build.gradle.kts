plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.simafiswrapper"
}

dependencies {
    runtimeOnly(libs.simmatcher)
}
