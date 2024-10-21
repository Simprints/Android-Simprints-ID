plugins {
    id("simprints.infra")
    id("simprints.library.realm")
}

android {
    namespace = "com.simprints.infra.realm"
}

dependencies {
    implementation(project(":infra:auth-store"))
}
configurations.all {
    // Realm uses old version of annotations library which conflicts with the one used by the app
    // More details here:https://github.com/realm/realm-java/issues/6153#issuecomment-991378320
    resolutionStrategy.force("org.jetbrains:annotations:26.0.1")
}
