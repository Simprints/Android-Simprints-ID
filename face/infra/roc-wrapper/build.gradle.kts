plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.feature.rocwrapper"
}

dependencies {
    implementation(project(":face:infra:face-bio-sdk"))
}
