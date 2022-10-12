plugins {
    id("com.android.library")
    id("kotlin-android")
    kotlin("kapt")
}

apply {
    from("profiling.gradle")
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        getByName(BuildParams.BuildTypes.profiling) {
            initWith(getByName(BuildParams.BuildTypes.debug))
        }
    }


}
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.simmatcher)
    implementation(libs.androidX.annotation.annotation)

    // Kotlin
    implementation(libs.kotlin.coroutinesAndroid)
    compileOnly(libs.androidX.annotation.annotation)

    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.kapt)

    // Mocking and assertion frameworks
    testImplementation(libs.testing.truth)
    testImplementation(libs.testing.mockk.core)
}
