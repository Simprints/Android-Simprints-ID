plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("$rootDir${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))

    implementation(Dependencies.AndroidX.core)
    implementation(Dependencies.AndroidX.appcompat)

    implementation(Dependencies.Firebase.crashlytics)
    implementation(Dependencies.Firebase.analytics)

    implementation(Dependencies.Timber.core)
    testImplementation(Dependencies.Testing.junit)
}
