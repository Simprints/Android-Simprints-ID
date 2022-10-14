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
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}

//Required to make the mock-android library to work in a no android module.
System.setProperty("org.mockito.mock.android", "true")

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))

    implementation(libs.androidX.lifecycle.livedata.ktx)
    implementation(libs.testing.androidX.core)
    implementation(libs.testing.androidX.core.testing)
    implementation(libs.androidX.multidex)
    implementation(libs.androidX.appcompat)

    implementation(libs.testing.junit)
    implementation(libs.testing.mockito.inline)
    implementation(libs.testing.mockito.kotlin)

    implementation(libs.testing.robolectric.core)
    implementation(libs.rxJava2.android)
    implementation(libs.rxJava2.core)
    implementation(libs.testing.coroutines.test)
    implementation(libs.testing.mockk.core)

}
