plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.setup"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:license"))
    implementation(project(":infra:auth-store"))
    implementation(project(":feature:alert"))

    implementation(libs.playServices.location)
    implementation(libs.workManager.work)
}
