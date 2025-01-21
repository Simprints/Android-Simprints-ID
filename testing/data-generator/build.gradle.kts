plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.simprints.feature.datagenerator"
}
dependencies {

    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:enrolment-records:repository"))
}
