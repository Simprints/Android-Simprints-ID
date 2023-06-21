import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Lists all plugins used throughout the project without applying them.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false

    // TODO Uncomment when issue with realm plugin is solved
    // alias(libs.plugins.realm) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.navigation.args) apply false

    alias(libs.plugins.gms) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.firebase.distribution) apply false
    alias(libs.plugins.play.publisher) apply false

    alias(libs.plugins.retry) apply false
    alias(libs.plugins.sonar) apply false
    alias(libs.plugins.depsGraph) apply false
}


// TODO Due to a bug either in plugin dsl or in the plugin packaging realm-android does not
//   resolve to correct path when added in plugins block abd build-logic dependencies.
//   This is temporary workaround until we find a way to add realm plugin or replace it with realm-kotlin.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.plugin.realm)
    }
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
    retry.maxRetries.set(5)
}
/*
Workaround to set jvmTarget = 11 because R8 doesn't yet support java 17
Should be removed before migrating to jdk 17
https://issuetracker.google.com/issues/212279104
 */
subprojects {
    tasks.withType(KaptGenerateStubsTask::class).configureEach {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
    tasks.withType(KotlinCompile::class).configureEach {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
}
