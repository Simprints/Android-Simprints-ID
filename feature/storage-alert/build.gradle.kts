plugins {
    id("simprints.feature")
}

android {
    namespace = "com.simprints.feature.storage.alert"
}

dependencies {
    implementation(project(":infra:config-store"))
}
