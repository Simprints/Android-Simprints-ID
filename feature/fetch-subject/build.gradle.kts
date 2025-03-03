plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
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
    implementation(project(":infra:config-sync"))
}
