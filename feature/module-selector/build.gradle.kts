plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.module.selector"
}

dependencies {
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:config-store"))
    implementation(project(":infra:sync"))
}
