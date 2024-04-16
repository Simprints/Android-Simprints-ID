plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.importsubject"
}

dependencies {

    implementation(project(":feature:alert"))

    implementation(project(":face:infra:bio-sdk-resolver"))

    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:license"))

}
