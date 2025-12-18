plugins {
    id("simprints.infra")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.simprints.fingerprint.infra.scanner"
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.reflect)

    implementation(libs.retrofit.core)
    runtimeOnly(libs.kotlin.serialization)

    implementation(project(":infra:config-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:recent-user-activity"))

    testImplementation(project(":fingerprint:infra:scannermock"))
}
