plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.polyprotect"
}

dependencies {

    implementation(project(":infra:realm"))
}
