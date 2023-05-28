plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.feature.rocwrapper"
}

dependencies {
    implementation(project(":infrafacebiosdk"))
}
