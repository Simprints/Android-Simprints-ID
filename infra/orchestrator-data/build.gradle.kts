plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.infra.orchestration.data"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:events"))
}
