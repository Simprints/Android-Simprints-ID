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
    namespace = "com.simprints.face"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":infra:config"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":infra:events"))
    implementation(project(":infralicense"))
    implementation(project(":infra:images"))
    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))

    //BIO SDK
    implementation(project(":infrafacebiosdk"))
    implementation(project(":infrarocwrapper"))

    implementation(libs.androidX.cameraX.core)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)
    implementation(libs.workManager.work)

    implementation(libs.circleImageView)

    runtimeOnly(libs.androidX.cameraX.core)

    // ######################################################
    //                      Android test
    // ######################################################

    // Navigation
    androidTestImplementation(libs.testing.navigation)
}
