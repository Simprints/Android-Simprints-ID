plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.validatepool"
}

dependencies {

    implementation(project(":infra:enrolment-records-store"))
}
