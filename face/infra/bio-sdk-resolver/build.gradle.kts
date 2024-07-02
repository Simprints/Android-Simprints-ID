plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.face.infra.biosdkresolver"
}
dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":face:infra:base-bio-sdk"))
    implementation(project(":face:infra:roc-v1"))
    api(project(":face:infra:roc-v3"))
}
