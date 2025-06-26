plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.face.infra.simface"
}

dependencies {
    implementation(project(":face:infra:base-bio-sdk"))
    api(libs.simface)

    api("com.google.mlkit:face-detection:16.1.7")
}
