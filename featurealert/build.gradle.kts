plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.alert"
}

dependencies {
    implementation(project(":infraevents"))
}
