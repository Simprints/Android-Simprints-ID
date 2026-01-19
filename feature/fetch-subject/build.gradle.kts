plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.fetchsubject"
}

dependencies {

    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))

    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
}
