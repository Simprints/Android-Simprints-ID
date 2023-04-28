plugins {
    id("simprints.android.application")
    id("kotlin-parcelize")

    id("simprints.ci.deploy")
    id("com.vanniktech.dependency.graph.generator")
}

android {
    namespace = "com.simprints.id"

    defaultConfig {
        ndk.abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
    }

    buildTypes {
        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "60L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "30L")
        }
        getByName("staging") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
        getByName("debug") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("long", "SYNC_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
            buildConfigField("long", "SECURITY_STATE_PERIODIC_WORKER_INTERVAL_MINUTES", "15L")
        }
    }
}

dependencies {
    // ######################################################
    //                     (Dependencies)
    // ######################################################

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":core"))

    implementation(project(":infraevents"))
    implementation(project(":infraeventsync"))
    implementation(project(":infralogin"))
    implementation(project(":clientapi"))
    implementation(project(":face"))
    implementation(project(":featuredashboard"))
    implementation(project(":featurealert"))
    implementation(project(":featureexitform"))
    implementation(project(":fingerprint"))
    implementation(project(":infraconfig"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infrarecentuseractivity"))
    implementation(project(":infraimages"))

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

    implementation(libs.androidX.cameraX.core)
    runtimeOnly(libs.androidX.cameraX.camera2)
    implementation(libs.androidX.cameraX.lifecycle)
    implementation(libs.androidX.cameraX.view)

    implementation(libs.workManager.work)
    implementation(libs.playServices.location)

    implementation(libs.rxJava2.core)
    implementation(libs.jackson.core)

    // Firebase
    implementation(libs.firebase.storage)
    implementation(libs.firebase.barcode)

    implementation(libs.retrofit.core)
    implementation(libs.jackson.core)

    // ######################################################
    //                      Unit test
    // ######################################################

    testImplementation(project(":testtools"))
    testImplementation(project(":fingerprintscannermock"))
    testImplementation(project(":infraevents"))
    testImplementation(project(":infraeventsync"))
    testImplementation(project(":infralogging"))

    testImplementation(libs.playServices.integrity)

    // ######################################################
    //                      Android test
    // ######################################################

    androidTestImplementation(project(":testtools"))
    androidTestImplementation(project(":fingerprintscannermock")) {
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
