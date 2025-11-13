plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.matcher"
}

dependencies {
    implementation(project(":feature:exit-form"))

    implementation(project(":infra:orchestrator-data"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:matching"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))

    implementation(project(":infra:auth-store"))
}
