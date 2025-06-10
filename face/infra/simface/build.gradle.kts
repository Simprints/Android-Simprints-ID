plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.face.infra.simface"
}

dependencies {
    implementation(project(":face:infra:base-bio-sdk"))
    api(libs.simface)
}
