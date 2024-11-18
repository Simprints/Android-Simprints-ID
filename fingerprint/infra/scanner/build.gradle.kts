plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.scanner"

}

dependencies {
    // Kotlin
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutine.rx2.adapter)

    implementation(libs.retrofit.core)
    runtimeOnly(libs.jackson.core)

    implementation(project(":infra:config-store"))
    implementation(project(":infra:config-sync"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:recent-user-activity"))

    // RxJava
    api(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)
    testImplementation(project(":fingerprint:infra:scannermock"))

}
