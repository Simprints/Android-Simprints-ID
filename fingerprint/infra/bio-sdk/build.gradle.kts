plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprints.infra.biosdk"
}

dependencies {
    implementation(project(":fingerprint:infra:scanner"))
    implementation(project(":infra:config-store"))
    api(project(":fingerprint:infra:base-bio-sdk"))
    implementation(project(":fingerprint:infra:simprints-bio-sdk"))
    implementation(project(":fingerprint:infra:nec-bio-sdk"))
}
