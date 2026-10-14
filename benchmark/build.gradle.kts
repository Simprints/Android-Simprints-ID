plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.simprints.testing.benchmark"

    compileSdk = 37
    defaultConfig {
        minSdk = 31
        targetSdk = 37

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your release build (for example, with minification on).
        // It's signed with a debug key for easy local/CI testing.
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    targetProjectPath = ":id"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.benchmark.macro.junit4)
    implementation(libs.testing.androidX.ext.junit)
    implementation(libs.testing.androidX.uiAutomator)
    implementation(libs.testing.espresso.core)
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}
