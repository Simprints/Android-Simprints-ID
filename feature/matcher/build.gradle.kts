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
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))

    implementation(project(":face:infra:face-bio-sdk"))
    implementation(project(":face:infra:facenet-wrapper"))

    implementation(project(":fingerprint:infra:bio-sdk"))
}
