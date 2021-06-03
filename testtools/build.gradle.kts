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

    api(Dependencies.AndroidX.multidex)
    implementation(Dependencies.Timber.core)
    implementation(Dependencies.Testing.AndroidX.monitor)
    implementation(Dependencies.Testing.AndroidX.core)
    api(Dependencies.Testing.Espresso.core)
    implementation(Dependencies.Testing.Espresso.barista) {
        exclude("com.android.support")
        exclude("com.google.code.findbugs")
        exclude("org.jetbrains.kotlin")
        exclude("com.google.guava")
    }

    annotationProcessor(Dependencies.Dagger.compiler)
    implementation(Dependencies.Kotlin.reflect)

    implementation(Dependencies.Testing.Mockito.inline)
    implementation(Dependencies.Testing.Mockito.kotlin)

    implementation(Dependencies.Testing.Robolectric.core)
    implementation(Dependencies.Testing.Robolectric.multidex)
    api(Dependencies.Testing.mockwebserver)
    implementation(Dependencies.RxJava2.android)
    api(Dependencies.RxJava2.core)
    api(Dependencies.Testing.retrofit)
    implementation(Dependencies.Retrofit.core)
    api(Dependencies.Testing.coroutines_test)
    implementation(Dependencies.Testing.Mockk.core)
}
