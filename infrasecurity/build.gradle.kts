plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("simprints.testing.unit")
}

android {
    namespace = "com.simprints.infra.security"
}

dependencies {
    implementation(project(":infra:logging"))
    implementation(libs.androidX.security)

    // RootBeer (root detection)
    implementation(libs.rootbeer.core)
}
