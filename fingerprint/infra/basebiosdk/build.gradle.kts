plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.basebiosdk"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}
dependencies{
    implementation(project(":fingerprint:infra:scanner"))
}
