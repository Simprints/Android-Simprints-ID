plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.enrollast"
}

dependencies {

    implementation(project(":feature:alert"))
    implementation(project(":feature:external-credential"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:events"))
    implementation(project(":infra:enrolment-records:repository"))
}
