plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
    id("simprints.library.kotlinSerialization")
}

android {
    namespace = "com.simprints.feature.externalcredential"
}

dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":infra:ui-base"))
    implementation(project(":feature:exit-form"))
    implementation(project(":infra:enrolment-records:repository"))
    implementation(project(":infra:matching"))
    implementation(project(":infra:events"))
    implementation(project(":infra:credential-store"))

    implementation(project(":infra:camera"))

    implementation(libs.mlkit.text.recognition)

    // ICAO 9303 document chip reading (BAC/PACE, secure messaging, LDS parsing). Brings BouncyCastle transitively.
    implementation(libs.jmrtd)
    implementation(libs.scuba.sc.android)
}
