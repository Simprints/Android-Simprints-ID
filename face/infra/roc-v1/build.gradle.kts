plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.face.infra.rocv1"
}

dependencies {
    implementation(project(":face:infra:base-bio-sdk"))
    implementation(libs.roc)
}
