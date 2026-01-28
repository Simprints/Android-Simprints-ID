plugins {
    id("simprints.infra")
    id("simprints.library.kotlinSerialization")
    id("simprints.library.backendApi")
}

android {
    namespace = "com.simprints.fingerprint.infra.scanner"
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.reflect)

    implementation(project(":infra:config-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:recent-user-activity"))

    testImplementation(project(":fingerprint:infra:scannermock"))
}
