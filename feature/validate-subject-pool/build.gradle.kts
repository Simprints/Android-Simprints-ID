plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.validatepool"
}

dependencies {

    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:events"))
    implementation(project(":infra:sync"))
}
