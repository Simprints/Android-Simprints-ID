plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.enrolment.records.store"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:realm"))

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
