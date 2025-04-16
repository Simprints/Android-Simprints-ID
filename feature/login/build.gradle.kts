plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.feature.login"
}

dependencies {

    implementation(project(":infra:auth-store"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infra:ui-base"))

    // Integrity check related
    implementation(libs.playServices.base)

}
