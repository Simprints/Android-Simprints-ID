plugins {
    id("simprints.feature")
}

android {
    namespace = "com.simprints.infra.credential.store"
}

dependencies {
    implementation(project(":infra:core"))
}
