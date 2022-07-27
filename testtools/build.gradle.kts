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
}

//Required to make the mock-android library to work in a no android module.
System.setProperty("org.mockito.mock.android", "true")

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))
    implementation(project(":infralogging"))

    implementation(libs.testing.androidX.core)
    api(libs.testing.espresso.core)
    implementation(libs.testing.espresso.barista) {
        exclude("com.android.support")
        exclude("com.google.code.findbugs")
        exclude("org.jetbrains.kotlin")
        exclude("com.google.guava")
    }

    implementation(libs.testing.mockito.inline)
    implementation(libs.testing.mockito.kotlin)

    implementation(libs.testing.robolectric.core)
    implementation(libs.rxJava2.android)
    api(libs.rxJava2.core)
    api(libs.testing.coroutines.test)
    implementation(libs.testing.mockk.core)

}
