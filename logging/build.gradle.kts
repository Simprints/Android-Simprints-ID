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

    // We specifically don't include Crashlytics, Analytics and Timber in the central buildSrc
    // module because we do not want or expect these dependencies to be used in multiple modules
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.1.0")
    implementation("com.google.firebase:firebase-analytics-ktx:19.0.0")

    //4.7.1 breaks realm:
    // https://github.com/realm/realm-java/issues/6153
    // https://github.com/JakeWharton/timber/issues/295
    implementation("com.jakewharton.timber:timber:4.5.1")

    // Unit Tests
    testImplementation(Dependencies.Testing.junit)
}
