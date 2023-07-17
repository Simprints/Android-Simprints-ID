plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.setup"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:config"))

    implementation(libs.playServices.location)
    implementation(libs.workManager.work)
}
