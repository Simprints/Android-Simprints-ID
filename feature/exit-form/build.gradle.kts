plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.exitform"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
}
