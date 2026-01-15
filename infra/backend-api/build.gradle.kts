plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.backendapi"
}

dependencies {
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:network"))
    implementation(libs.retrofit.core)
}
