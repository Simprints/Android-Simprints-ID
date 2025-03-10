plugins {
    id("simprints.infra")
}

android {
    namespace = "com.simprints.ears.infra.biosdkresolver"
}
dependencies {
    implementation(project(":infra:config-store"))
    implementation(project(":ear:infra:base-bio-sdk"))
    implementation(project(":ear:infra:ear-simface-sdk"))
}
