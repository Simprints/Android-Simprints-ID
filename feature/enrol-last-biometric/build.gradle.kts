plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.enrollast"
}

dependencies {

    implementation(project(":feature:alert"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records:repository"))
}
