plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.selectagegroup"
}

dependencies {
    implementation(project(":infra:events"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:logging"))
    implementation(project(":infra:config-store"))
    implementation(project(":feature:exit-form"))
}
