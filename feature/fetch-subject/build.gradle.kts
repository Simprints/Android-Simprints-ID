plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.fetchsubject"
}

dependencies {

    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))

    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraeventsync"))
    implementation(project(":infraevents"))
    implementation(project(":infraconfig"))

}
