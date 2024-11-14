plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.fingerprint.connect"
}

dependencies {

    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:events"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))

    implementation(project(":fingerprint:infra:scanner"))

    testImplementation(project(":fingerprint:infra:scannermock"))
    // Uncomment the line below to use the mock scanner implementation
    // implementation(project(":fingerprint:infra:scannermock"))
}
