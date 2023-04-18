plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.infra.license"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":infralogin"))

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)
    implementation(libs.androidX.security)
}
