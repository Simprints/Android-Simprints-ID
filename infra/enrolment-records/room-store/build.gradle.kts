plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.infra.enrolment.records.realm.store"
}

dependencies {
    implementation(project(":infra:auth-store"))
}
