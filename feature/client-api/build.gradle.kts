plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}
android {
    namespace = "com.simprints.feature.clientapi"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:enrolment-records-sync"))

    implementation(libs.libsimprints)
    implementation(project(":infra:orchestrator-data"))
}
