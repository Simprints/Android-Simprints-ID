plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.selectsubject"
}

dependencies {
    implementation(project(":infra:external-credential-store"))
    implementation(project(":infra:events"))
    implementation(project(":infra:auth-store"))
}
