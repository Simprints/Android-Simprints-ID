plugins {
    id("simprints.infra")
    id("simprints.config.cloud")
}

android {
    namespace = "com.simprints.infra.authlogic"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:events"))

    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:images"))
    implementation(project(":infra:recent-user-activity"))
    implementation(project(":infra:license"))

    implementation(project(":fingerprint:infra:scanner"))

    implementation(libs.retrofit.core)
    implementation(libs.playServices.integrity)
    implementation(libs.workManager.work)
}
