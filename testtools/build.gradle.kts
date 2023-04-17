plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    namespace = "com.simprints.testtools"
}

//Required to make the mock-android library to work in a no android module.
System.setProperty("org.mockito.mock.android", "true")

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":core"))

    implementation(libs.testing.androidX.core.testing)
    api(libs.androidX.multidex)
    api(libs.androidX.appcompat)

    api(libs.testing.junit)
    implementation(libs.testing.mockito.inline)
    implementation(libs.testing.mockito.kotlin)

    implementation(libs.testing.robolectric.core)
    implementation(libs.rxJava2.android)
    api(libs.rxJava2.core)
    implementation(libs.testing.coroutines.test)
    implementation(libs.testing.mockk.core)

}
