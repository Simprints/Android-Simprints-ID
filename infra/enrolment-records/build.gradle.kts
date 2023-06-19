plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.enrolment.records"
}

dependencies {
    implementation(project(":infra:config"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:realm"))

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    implementation(libs.workManager.work)
}
