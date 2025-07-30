plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.consent"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:events"))
    implementation(project(":feature:exit-form"))

    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)

    implementation(libs.mlkit.entity.extraction)
    implementation(libs.mlkit.text.recognition)
}
