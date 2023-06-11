plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.fetchsubject"
}

dependencies {

    implementation(project(":featurealert"))
    implementation(project(":featureexitform"))

    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraeventsync"))
    implementation(project(":infraevents"))
    implementation(project(":infraconfig"))

}
