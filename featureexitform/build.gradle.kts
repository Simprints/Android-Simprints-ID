plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.exitform"
}

dependencies {
    implementation(project(":infraevents"))
}
