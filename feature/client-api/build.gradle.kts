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
    implementation(project(":feature:login"))

    implementation(project(":infra:config"))
    implementation(project(":infra:events"))
    implementation(project(":infra:security"))
    implementation(project(":infra:project-security-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":moduleapi"))

    implementation(libs.libsimprints)
}
