plugins {
    id("simprints.infra")
    id("simprints.library.realm")
}

android {
    namespace = "com.simprints.infra.realm"
}

dependencies {
    implementation(project(":infra:auth-store"))
}
