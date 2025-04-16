plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.externalcredential"
}

dependencies {
    implementation(project(":infra:external-credential-store"))
    implementation(project(":infra:ui-base"))
    implementation(project(":feature:exit-form"))
}
