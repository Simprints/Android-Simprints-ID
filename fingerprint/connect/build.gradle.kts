plugins {
    id("simprints.feature")
    id("kotlin-parcelize")
}

android {
    namespace = "com.simprints.fingerprint.connect"
}

dependencies {

    implementation(project(":infra:config"))
    implementation(project(":infra:events"))
    implementation(project(":infra:recent-user-activity"))

    implementation(project(":feature:alert"))
    implementation(project(":feature:exit-form"))

    implementation(project(":fingerprint:infra:scanner"))

}
