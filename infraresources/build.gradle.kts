plugins {
    id("simprints.android.library")
}

sonarqube {
    /*
     * We skip the infraresources module because it has no source code to analyse. This should be
     * removed if that ever changes
     */
    isSkipProject = true
}

android {
    namespace = "com.simprints.infra.resources"
}

dependencies {
    implementation(libs.support.material)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.ui.constraintlayout)

    implementation(libs.androidX.ui.preference) {
        exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx")
    }
}
