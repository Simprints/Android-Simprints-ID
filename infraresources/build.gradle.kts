plugins {
    id("com.android.library")
    id("kotlin-android")
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
    implementation(libs.support.material)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.ui.preference)

    // This implementation is to fix the issue with duplicated class on the view model
    implementation(libs.androidX.lifecycle.viewmodel)
}
