plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    namespace = "com.simprints.infra.enrolment.records"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":core"))
    api(project(":infraconfig"))
    implementation(project(":infralogging"))
    api(project(":infralogin"))
    implementation(project(":infranetwork"))
    api(project(":infrarealm"))
    api(project(":moduleapi"))

    implementation(libs.hilt)
    api(libs.hilt.work)
    kapt(libs.hilt.kapt)
    kapt(libs.hilt.compiler)
    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    implementation(libs.workManager.work)
    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines.test)
}
