plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.feature.rocwrapper"
}

dependencies {
    implementation(project(":face:infra:base-bio-sdk"))
    implementation(libs.roc)
}
