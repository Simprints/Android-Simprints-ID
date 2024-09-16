plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.face.infra.rocv3"
}

dependencies {
    api(project(":face:infra:base-bio-sdk"))
    implementation(libs.roc.v3)
}
