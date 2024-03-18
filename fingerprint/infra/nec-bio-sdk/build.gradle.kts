plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.necbiosdk"
}

dependencies {
    implementation(project(":fingerprint:infra:scanner"))
    implementation(project(":infra:license"))
    implementation(project(":infra:security"))
    implementation(project(":infra:recent-user-activity"))
    api(project(":fingerprint:infra:base-bio-sdk"))

    //NEC SDK lib and wrapper
    implementation(libs.nec.wrapper)
    implementation(libs.nec.lib)
    // Image processing libs
    // WsqDecoder and BitmapConverter libs are very old and we should consider replacing them but
    // they are the only ones that works with wsq images
    implementation(libs.secugen)
    implementation(libs.wsqDecoder)
    implementation(libs.bitmapConverter){
        exclude("com.android.support")
    }
}
