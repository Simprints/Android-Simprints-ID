plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
    id("simprints.testing.android")
}

android {
    namespace = "com.simprints.infra.enrolment.records.repository"
    defaultConfig {
        testInstrumentationRunner = "com.simprints.infra.enrolment.records.repository.local.HiltTestRunner"
    }
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:enrolment-records:realm-store"))
    implementation(project(":infra:enrolment-records:room-store"))
    implementation(project(":infra:events"))

    implementation(libs.libsimprints)
    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
}
