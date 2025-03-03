plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.config.sync"
}

dependencies {

    implementation(project(":infra:config-store"))
    implementation(project(":infra:enrolment-records:repository"))
}
