plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.exitform"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
}
