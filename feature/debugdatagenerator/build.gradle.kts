plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.debugdatagenerator"
}
dependencies {

    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:sync"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:images"))
    implementation(project(":feature:orchestrator"))
    implementation(project(":infra:orchestrator-data"))
    implementation(project(":fingerprint:capture"))
    implementation(project(":face:capture"))
}
