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
    implementation(libs.androidX.ui.preference){
        exclude("androidx.lifecycle","lifecycle-viewmodel-ktx")
    }
}
