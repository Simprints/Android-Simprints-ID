plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.fingerprint.infra.basebiosdk"

}

dependencies {
    implementation(project(":fingerprint:infra:scanner"))
}
