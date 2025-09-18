plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.externalcredential"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":feature:exit-form"))
}
