plugins {
    id("simprints.infra")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.infra.enrolment.records"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":infraconfig"))
    implementation(project(":infralogin"))
    implementation(project(":infrarealm"))

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    implementation(libs.workManager.work)
}
