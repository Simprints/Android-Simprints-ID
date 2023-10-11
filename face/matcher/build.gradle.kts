plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.face.matcher"
}

dependencies {

    implementation(project(":infra:enrolment-records"))
    implementation(project(":infra:events"))
    implementation(project(":infra:config"))

    implementation(project(":face:infra:face-bio-sdk"))
    implementation(project(":face:infra:roc-wrapper"))

    implementation(project(":fingerprint:infra:bio-sdk"))
}
