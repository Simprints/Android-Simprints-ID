plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.enrolment.records.sync"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:realm"))

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    implementation(libs.workManager.work)
}
