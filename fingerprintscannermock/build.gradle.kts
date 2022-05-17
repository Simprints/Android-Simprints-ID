plugins {
    id("com.android.library")
    id("com.jfrog.artifactory")
    id("kotlin-android")
    id("maven-publish")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Simprints
    api(project(":fingerprintscanner"))

    // Reactive
    api(libs.rxJava2.core)
    implementation(libs.rxJava2.kotlin)

    // For Tee-ing output streams
    implementation(libs.commonsIO.commons.io)

    // Unit tests
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.robolectric.core)
    testImplementation(libs.rxJava2.android)
}
