plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.face.infra.rocv1"
}

dependencies {
    // Use API dependencies to ensure that the dependencies are exposed to consumers of the library
    api(project(":face:infra:base-bio-sdk"))
    implementation(libs.roc.v1)
}
