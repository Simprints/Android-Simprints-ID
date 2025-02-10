plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.scanner"
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.reflect)

    implementation(libs.retrofit.core)
    runtimeOnly(libs.jackson.core)

    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:recent-user-activity"))

    testImplementation(project(":fingerprint:infra:scannermock"))
}
