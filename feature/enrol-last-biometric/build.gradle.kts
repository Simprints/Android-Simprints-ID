plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.enrollast"
}

dependencies {

    implementation(project(":featurealert"))

    implementation(project(":infraconfig"))
    implementation(project(":infraevents"))

}
