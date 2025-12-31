plugins {
    id("simprints.infra")
    id("simprints.library.room")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.simprints.infra.events"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:credential-store"))

    implementation(libs.jackson.core)
    implementation(libs.kotlin.serialization)

    implementation(libs.workManager.work)

//    androidTestImplementation(project("infra:enrolment-records:repository"))
}
