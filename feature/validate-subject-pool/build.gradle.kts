plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.validatepool"
}

dependencies {

    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:events"))
    implementation(project(":infra:sync"))
}
