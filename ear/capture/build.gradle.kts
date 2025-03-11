plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

sonarqube {
    properties {
        property("sonar.sources", "src/main/java")
    }
}

android {
    namespace = "com.simprints.ear.capture"
}

dependencies {
    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:events"))
    implementation(project(":infra:images"))
    implementation(project(":infra:resources"))
    implementation(project(":feature:exit-form"))
    implementation(project(":infra:license"))
    implementation(project(":feature:alert"))

    // BIO SDK
    implementation(project(":ear:infra:bio-sdk-resolver"))

    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)
    implementation(libs.androidX.ui.preference)
    implementation(libs.workManager.work)

    implementation(libs.circleImageView)

    runtimeOnly(libs.androidX.cameraX.core)

    // Navigation
    androidTestImplementation(libs.testing.navigation)
}
