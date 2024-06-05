plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.orchestrator"
}

dependencies {

    implementation(project(":feature:login-check"))
    implementation(project(":feature:client-api"))
    implementation(project(":feature:alert"))
    implementation(project(":feature:login"))
    implementation(project(":feature:setup"))
    implementation(project(":feature:consent"))
    implementation(project(":feature:enrol-last-biometric"))
    implementation(project(":feature:fetch-subject"))
    implementation(project(":feature:select-subject"))
    implementation(project(":feature:exit-form"))
    implementation(project(":feature:matcher"))
    implementation(project(":feature:validate-subject-pool"))
    implementation(project(":feature:select-subject-age-group"))

    implementation(project(":face:capture"))

    implementation(project(":fingerprint:connect"))
    implementation(project(":fingerprint:capture"))

    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:recent-user-activity"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:images"))

    implementation(libs.jackson.core)
}
