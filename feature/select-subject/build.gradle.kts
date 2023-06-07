plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.selectsubject"
}

dependencies {
    implementation(project(":infraevents"))
    implementation(project(":infra:auth-store"))
}
