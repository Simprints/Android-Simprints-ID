plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

sonarqube {
    properties {
        property("sonar.sources", "src/main/java")
    }
}

android {
    namespace = "com.simprints.clientapi"
}

dependencies {
    implementation(project(":infra:ui-base"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:enrolment-records-sync"))
    implementation(project(":infra:enrolment-records-store"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:events"))
    implementation(project(":feature:alert"))
    implementation(project(":infra:auth-store"))

    implementation(libs.libsimprints)
}
