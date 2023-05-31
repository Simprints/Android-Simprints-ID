plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.enrolment.records"
}

dependencies {
    implementation(project(":infraconfig"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infrarealm"))

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    implementation(libs.workManager.work)
}
