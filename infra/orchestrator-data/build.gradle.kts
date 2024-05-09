plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.orchestration.data"
}

dependencies {

    implementation(project(":infra:config-store"))
    implementation(project(":infra:events"))

    implementation(project(":feature:exit-form"))

    implementation(libs.jackson.core)
}
