plugins {
    id("simprints.android.library")
    id("simprints.library.hilt")
    id("simprints.config.network")
}

android {
    namespace = "com.simprints.infra.network"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":infralogging"))

    debugImplementation(libs.chuck.debug) {
        exclude("androidx.lifecycle", "lifecycle-viewmodel-ktx")
    }
    implementation(libs.chuck.release)

    implementation(libs.jackson.core)

    runtimeOnly(libs.kotlin.coroutinesAndroid)

    implementation(libs.retrofit.adapter)
    implementation(libs.retrofit.converterScalars)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.jackson)
    implementation(libs.retrofit.logging)
    implementation(libs.retrofit.okhttp)

    // Unit Tests
    testImplementation(libs.testing.junit)
    testImplementation(libs.testing.mockk.core)
    testImplementation(libs.testing.coroutines)
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.koTest.kotlin.assert)
    testImplementation(libs.testing.androidX.runner)

    testImplementation(libs.testing.mockwebserver)
    testImplementation(libs.chuck.release)

}

configurations {
    debugImplementation {
        // We have two versions of chucker, a dummy one "library-no-op" that is designed for release and staging build types
        // And a full feature version that should be added in debug build types
        exclude("com.github.chuckerteam.chucker", "library-no-op")
    }
    testImplementation {
        exclude("com.github.chuckerteam.chucker", "library")
    }
}
