plugins {
    id("com.android.library")
    kotlin("android")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation(project(mapOf("path" to ":core")))

    // Unit Tests
    testImplementation(libs.testing.junit)
}
