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
    implementation(libs.secugen)

}
