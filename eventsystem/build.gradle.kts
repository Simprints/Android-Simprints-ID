plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Dependencies.Kotlin.coroutines_android)
    compileOnly(Dependencies.AndroidX.Annotation.annotation)

    testImplementation(Dependencies.Testing.junit)
    androidTestImplementation(Dependencies.Testing.AndroidX.core_testing)
    androidTestImplementation(Dependencies.Testing.AndroidX.runner)
    androidTestImplementation(Dependencies.Testing.AndroidX.ext_junit)
}
