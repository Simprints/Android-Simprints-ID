import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
    id("org.sonarqube")
}

sonarqube {
    properties {
        property("sonar.sources", "src/main/java")
    }
}

android {
    namespace = "com.simprints.face"

    ndkVersion = gradleLocalProperties(rootDir).getProperty("ndk.Version")
        ?: System.getenv("ndk.Version")

    externalNativeBuild {
        ndkBuild.path("jni/Application.mk")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraevents"))
    implementation(project(":infralicense"))
    implementation(project(":infraimages"))
    implementation(project(":featurealert"))

    implementation(libs.cameraView)
    implementation(libs.circleImageView)

    runtimeOnly(libs.androidX.cameraX.core)

    // ######################################################
    //                      Android test
    // ######################################################

    // Navigation
    androidTestImplementation(libs.testing.navigation)
}
