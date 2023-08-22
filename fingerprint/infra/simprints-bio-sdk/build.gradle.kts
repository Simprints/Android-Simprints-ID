plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.biosdkimpl"
}

dependencies {
    implementation(project(":fingerprint:infra:scanner"))
    api(project(":fingerprint:infra:base-bio-sdk"))// base sdk is an api dependency as it is used by the client
    implementation(project(":fingerprint:infra:simafis-wrapper"))
}
