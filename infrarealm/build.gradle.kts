plugins {
    id("simprints.infra")
    id("simprints.library.realm")
}

android {
    namespace = "com.simprints.infra.realm"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":infralogin"))
}
