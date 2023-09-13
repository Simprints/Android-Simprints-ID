plugins {
    id("simprints.feature")
}

android {
    namespace = "com.simprints.feature.orchestrator"
}

dependencies {

    implementation(project(":feature:login-check"))
    implementation(project(":feature:client-api"))
    implementation(project(":feature:alert"))
    implementation(project(":feature:login"))

    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:events"))
}
