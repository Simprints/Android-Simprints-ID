plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("simprints.testing.unit")
}

android {
    namespace = "com.simprints.moduleapi"
}

dependencies {
    implementation(libs.androidX.annotation.annotation)
}
