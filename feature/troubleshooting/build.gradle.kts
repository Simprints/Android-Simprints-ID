plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.troubleshooting"
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:events"))
    //implementation(project(":infra:event-sync"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:license"))
    implementation(project(":infra:network"))

    implementation(project(":fingerprint:infra:scanner"))

    implementation(libs.workManager.work)
}
