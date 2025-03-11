plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.ear.infra.earsdk"
}

dependencies {
    // Use API dependencies to ensure that the dependencies are exposed to consumers of the library
    api(project(":ear:infra:base-bio-sdk"))
    implementation(libs.simface)

    implementation("com.google.mlkit:face-detection:16.1.6")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.4.4")
}
