plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

apply {
    from("$rootDir${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    namespace = "com.simprints.infra.security"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":infralogging"))

    api(libs.androidX.security)

    // RootBeer (root detection)
    implementation(libs.rootbeer.core)

    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Unit Tests
    testImplementation(project(":testtools"))

    testImplementation(libs.testing.androidX.ext.junit)
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.truth)
}
