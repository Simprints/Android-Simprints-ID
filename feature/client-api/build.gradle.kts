plugins {
    id("simprints.feature")
}
android {
    namespace = "com.simprints.feature.clientapi"
}

dependencies {
    implementation(project(":feature:orchestrator"))
    implementation(project(":feature:alert"))

    implementation(project(":infra:events"))

    implementation(libs.libsimprints)
}
