plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}
android {
    namespace = "com.simprints.feature.clientapi"
}

dependencies {

    implementation(project(":infra:config-store"))
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:logging-persistent"))

    implementation(libs.libsimprints)
}
