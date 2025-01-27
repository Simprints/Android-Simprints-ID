// Lists all plugins used throughout the project without applying them.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false

    alias(libs.plugins.realm) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.navigation.args) apply false
    alias(libs.plugins.room) apply false

    alias(libs.plugins.gms) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.play.publisher) apply false

    alias(libs.plugins.sonar) apply false
    alias(libs.plugins.depsGraph) apply false
}

apply {
    from("build-logic${File.separator}sonarqube.gradle")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
/*
Run tests in parallel to speed up tests
https://docs.gradle.org/nightly/userguide/performance.html#suggestions_for_java_projects
*/
tasks.withType(Test::class).configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
}
