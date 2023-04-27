plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprintscannermock"
}

dependencies {
    // Simprints
    api(project(":fingerprintscanner"))

    // Reactive
    api(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)

    // For Tee-ing output streams
    implementation(libs.commonsIO.commons.io)
}
