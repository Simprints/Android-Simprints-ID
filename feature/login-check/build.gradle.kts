plugins {
    id("simprints.feature")
}

android {
    namespace = "com.simprints.feature.logincheck"
}

dependencies {

    implementation(project(":feature:login"))

    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:config"))
    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:images"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:project-security-store"))
    implementation(project(":infra:recent-user-activity"))
    implementation(project(":infra:enrolment-records"))
}