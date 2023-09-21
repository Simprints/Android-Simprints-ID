plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.face.configuration"
}

dependencies {

    implementation(project(":face:infra:face-bio-sdk"))

    implementation(project(":feature:alert"))

    implementation(project(":infra:license"))
    implementation(project(":infra:events"))
}
