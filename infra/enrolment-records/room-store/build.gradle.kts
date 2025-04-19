plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.infra.enrolment.records.room.store"
}

dependencies {
    implementation(project(":infra:auth-store"))
}
