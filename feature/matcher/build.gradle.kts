plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.matcher"
}

dependencies {

    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))

    implementation(project(":face:infra:bio-sdk-resolver"))

    implementation(project(":document:infra:document-sdk-resolver"))

    implementation(project(":fingerprint:infra:bio-sdk"))
    implementation(project(":infra:auth-store"))
}
