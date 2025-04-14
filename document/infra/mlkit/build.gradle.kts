plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.document.infra.mlkit"
}

dependencies {
    // Use API dependencies to ensure that the dependencies are exposed to consumers of the library
    api(project(":document:infra:base-document-sdk"))
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:entity-extraction:16.0.0-beta5")
}
