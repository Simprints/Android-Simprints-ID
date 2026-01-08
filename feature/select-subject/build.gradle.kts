plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.selectsubject"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:config-store"))
    implementation(project(":feature:external-credential"))
}
