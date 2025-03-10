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
}
