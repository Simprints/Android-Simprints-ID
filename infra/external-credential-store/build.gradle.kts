plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
    id("simprints.library.realm")
}

android {
    namespace = "com.simprints.infra.external.credential.store"
}

dependencies {
    implementation(project(":infra:enrolment-records:realm-store"))
}
