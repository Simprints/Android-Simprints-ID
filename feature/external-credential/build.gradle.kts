plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.externalcredential"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:ui-base"))
    implementation(project(":feature:exit-form"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:matching"))
    implementation(libs.androidX.cameraX.view)
    implementation(libs.mlkit.text.recognition)
}
