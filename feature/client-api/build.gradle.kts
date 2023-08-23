plugins {
    id("simprints.feature")
}
android {
    namespace = "com.simprints.feature.clientapi"
}

dependencies {
    implementation(project(":feature:orchestrator"))
}
