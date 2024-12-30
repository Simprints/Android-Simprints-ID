plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.scannermock"
}

dependencies {
    // Simprints
    api(project(":fingerprint:infra:scanner"))
    // For Tee-ing output streams
    implementation(libs.commonsIO.commons.io)
}
