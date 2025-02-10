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
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:license"))
    implementation(project(":infra:network"))
    implementation(project(":infra:logging-persistent"))

    implementation(project(":fingerprint:infra:scanner"))

    implementation(libs.workManager.work)
    implementation(libs.zip4j)
}
