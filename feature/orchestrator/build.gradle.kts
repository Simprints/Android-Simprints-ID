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

    implementation(project(":face:configuration"))
    implementation(project(":face:capture"))
    implementation(project(":face:matcher"))

    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":infra:config"))
    implementation(project(":infra:events"))
}
