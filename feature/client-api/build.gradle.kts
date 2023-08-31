plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}
android {
    namespace = "com.simprints.feature.clientapi"
}

dependencies {
    implementation(project(":feature:orchestrator"))
    implementation(project(":feature:alert"))

    implementation(project(":infra:config"))
    implementation(project(":infra:events"))
    implementation(project(":infra:security"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":moduleapi"))

    implementation(libs.libsimprints)
}
