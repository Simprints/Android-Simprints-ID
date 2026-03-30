plugins {
    id("simprints.feature")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.storage.alert"
}

dependencies {
    implementation(project(":infra:config-store"))
}
