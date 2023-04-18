plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("simprints.testing.unit")
}

android {
    namespace = "com.simprints.infra.security"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":infralogging"))
    implementation(libs.androidX.security)

    // RootBeer (root detection)
    implementation(libs.rootbeer.core)
}
