plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.document.infra.documentsdkresolver"
}
dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":document:infra:base-document-sdk"))
    implementation(project(":document:infra:mlkit"))
}
