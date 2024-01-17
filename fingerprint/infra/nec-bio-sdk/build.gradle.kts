plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.necbiosdk"
}

dependencies {
    implementation(project(":fingerprint:infra:scanner"))
    implementation(project(":infra:license"))
    implementation(project(":infra:auth-store"))
    api(project(":fingerprint:infra:base-bio-sdk"))

    implementation(libs.nec.wrapper)
    implementation(libs.nec.lib)

    implementation(libs.secugen)
    implementation(libs.wsqDecoder)
    implementation(libs.bitmapConverter){
        exclude("com.android.support")
    }
}
