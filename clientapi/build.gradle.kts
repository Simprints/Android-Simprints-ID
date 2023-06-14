plugins {
    id("simprints.feature")
    id("simprints.testing.android")
    id("kotlin-parcelize")
}

sonarqube {
    properties {
        property("sonar.sources", "src/main/java")
    }
}

android {
    namespace = "com.simprints.clientapi"
}

dependencies {
    implementation(project(":infrauibase"))
    implementation(project(":infra:config"))
    implementation(project(":infraenrolmentrecords"))
    implementation(project(":infraeventsync"))
    implementation(project(":infraevents"))
    implementation(project(":feature:alert"))

    implementation(libs.libsimprints)
}
