plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.setup"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:license"))
    implementation(project(":infra:auth-store"))
    implementation(project(":feature:alert"))

    implementation(libs.playServices.location)
    implementation(libs.workManager.work)
}
