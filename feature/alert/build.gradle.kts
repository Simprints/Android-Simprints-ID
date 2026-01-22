plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.alert"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:auth-store"))
}
