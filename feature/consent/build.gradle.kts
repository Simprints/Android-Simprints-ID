plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.consent"
}

dependencies {
    implementation(project(":infra:config"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:events"))
    implementation(project(":feature:exit-form"))
}
