plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.scannermock"
}

dependencies {
    // Simprints
    api(project(":fingerprint:infra:scanner"))

    // Reactive
    api(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)

    // For Tee-ing output streams
    implementation(libs.commonsIO.commons.io)
}
