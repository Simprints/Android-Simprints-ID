plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("simprints.testing.unit")
}

android {
    namespace = "com.simprints.moduleapi"

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}

dependencies {
    implementation(libs.androidX.annotation.annotation)
}
