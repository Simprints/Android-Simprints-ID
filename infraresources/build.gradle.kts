plugins {
    id("simprints.android.library")
}

android {
    namespace = "com.simprints.infra.resources"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.support.material)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.ui.constraintlayout)

    implementation(libs.androidX.ui.preference) {
        exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx")
    }
}
