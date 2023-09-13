plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}
android {
    namespace = "com.simprints.feature.clientapi"
}

dependencies {

    implementation(project(":infra:config"))
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records"))

    implementation(libs.libsimprints)
    implementation(project(":infra:orchestrator-data"))
}
