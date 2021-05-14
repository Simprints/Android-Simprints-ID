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
    api(Dependencies.RxJava2.core)
    implementation(Dependencies.RxJava2.kotlin)

    // For Tee-ing output streams
    implementation(Dependencies.CommonsIO.commons_io)

    // Unit tests
    testImplementation(project(":testtools"))
    testImplementation(Dependencies.Testing.junit)
    testImplementation(Dependencies.Testing.Robolectric.core)
    testImplementation(Dependencies.RxJava2.android)
}
