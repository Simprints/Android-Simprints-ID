plugins {
    id("simprints.infra")
    id("simprints.library.room")
}

android {
    namespace = "com.simprints.infra.events"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":infraconfig"))
    implementation(project(":infralogin"))

    implementation(libs.jackson.core)

    implementation(libs.workManager.work)

    androidTestImplementation(project(":infraenrolmentrecords"))
}

