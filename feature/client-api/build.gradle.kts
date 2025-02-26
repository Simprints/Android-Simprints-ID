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
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:logging-persistent"))

    implementation(libs.libsimprints)
    implementation(libs.jackson.core)
}
