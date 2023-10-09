plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprints.infra.biosdk"
}

dependencies {
    implementation(project(":fingerprint:infra:scanner"))

    api(project(":fingerprint:infra:base-bio-sdk"))
    implementation(project(":fingerprint:infra:simprints-bio-sdk"))
}
