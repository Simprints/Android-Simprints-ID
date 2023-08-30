import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("simprints.android.application")
    id("kotlin-parcelize")

    id("simprints.ci.deploy")
    id("com.vanniktech.dependency.graph.generator")
}

android {
    namespace = "com.simprints.id"

    lint {
        //suppress false positives lint rules
        disable += setOf("BadConfigurationProvider", "Instantiatable")
    }
    defaultConfig {
        ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
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
    // ######################################################
    //                     (Dependencies)
    // ######################################################

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))

    // Since the new orchestrator is not yet ready for production it must be explicitly enabled
    // by setting the USE_NEW_ORCHESTRATOR=true in the local.properties file.
    // TODO: Remove once orchestrator is done - https://simprints.atlassian.net/browse/CORE-2845
    if (gradleLocalProperties(rootDir)["USE_NEW_ORCHESTRATOR"] == "true") {
        implementation(project(":feature:client-api"))
    } else {
        implementation(project(":clientapi"))
    }

    implementation(project(":infra:ui-base"))
    implementation(project(":infra:events"))
    implementation(project(":infra:event-sync"))
    implementation(project(":infra:auth-logic"))
    implementation(project(":infra:auth-store"))
    implementation(project(":infra:project-security-store"))

    implementation(project(":face"))
    implementation(project(":feature:login"))
    implementation(project(":feature:fetch-subject"))
    implementation(project(":feature:select-subject"))
    implementation(project(":feature:enrol-last-biometric"))
    implementation(project(":feature:setup"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))
    implementation(project(":feature:consent"))
    implementation(project(":fingerprint:controller"))
    implementation(project(":infra:config"))
    implementation(project(":infra:enrolment-records"))
    implementation(project(":infra:recent-user-activity"))
    implementation(project(":infra:images"))

    implementation(libs.kotlin.reflect)
    runtimeOnly(libs.kotlin.coroutinesAndroid)

    implementation(libs.androidX.core)
    implementation(libs.androidX.appcompat)
    implementation(libs.androidX.ui.activity)
    implementation(libs.androidX.ui.fragment)
    implementation(libs.androidX.ui.constraintlayout)
    implementation(libs.androidX.ui.cardview)
    implementation(libs.support.material)

    implementation(libs.androidX.lifecycle)
    implementation(libs.androidX.lifecycle.scope)
    implementation(libs.androidX.lifecycle.livedata.ktx)

    implementation(libs.workManager.work)

    implementation(libs.rxJava2.core)

    // ######################################################
    //                      Unit test
    // ######################################################

    testImplementation(project(":testtools"))
    testImplementation(project(":infra:events"))
    testImplementation(project(":infra:event-sync"))
    testImplementation(project(":infra:logging"))

    // ######################################################
    //                      Android test
    // ######################################################

    androidTestImplementation(project(":testtools"))
    androidTestImplementation(project(":fingerprint:infra:scannermock")) {
        exclude("org.robolectric")
    }
    androidTestUtil(libs.testing.androidX.orchestrator)
    androidTestImplementation(libs.testing.espresso.barista) {
        exclude("com.android.support")
        exclude("com.google.code.findbugs")
        exclude("org.jetbrains.kotlin")
        exclude("com.google.guava")
    }
}
