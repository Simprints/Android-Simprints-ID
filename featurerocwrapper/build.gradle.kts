plugins {
    id("simprints.feature")
}

android {
    namespace = "com.simprints.feature.rocwrapper"
}

dependencies {
    implementation(project(":infrafacebiosdk"))
}
