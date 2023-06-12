plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.scanner"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    // RxJava
    api(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)
}
