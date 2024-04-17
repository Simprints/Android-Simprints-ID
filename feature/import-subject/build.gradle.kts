plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.importsubject"
}

dependencies {

    implementation(project(":feature:alert"))

    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))

}
