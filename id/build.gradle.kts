import common.SdkVersions

plugins {
    id("simprints.android.application")
    id("simprints.ci.deploy")
    id("com.vanniktech.dependency.graph.generator")
}

android {
    namespace = "com.simprints.id"

    defaultConfig {
        ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
    }

    compileOptions {
        sourceCompatibility = SdkVersions.JAVA_TARGET
        targetCompatibility = SdkVersions.JAVA_TARGET
        isCoreLibraryDesugaringEnabled = true
    }

    buildTypes {
        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("staging") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {

    implementation(project(":infra:core"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:sync"))
    implementation(project(":infra:enrolment-records:repository"))

    implementation(project(":feature:orchestrator"))
    implementation(project(":feature:dashboard"))

    implementation(libs.androidX.core)
    implementation(libs.androidX.appcompat)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
