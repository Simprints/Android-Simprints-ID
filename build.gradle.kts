import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

apply {
    from("ci${File.separator}scanning${File.separator}sonarqube.gradle")
    from("ci${File.separator}scanning${File.separator}dependency_health.gradle")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://storage.googleapis.com/r8-releases/raw/master")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://kotlin.bintray.com/kotlinx/")
    }

    dependencies {
        // Gradle & Kotlin
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.version.get()}")

        // CI Scanning & Retry
        classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")

        classpath("org.jacoco:org.jacoco.core:${Plugins.jacoco}")

        classpath("org.ow2.asm:asm:9.3")
        classpath("com.autonomousapps:dependency-analysis-gradle-plugin:0.80.0")
        classpath("org.gradle:test-retry-gradle-plugin:1.4.0")

        // Firebase
        classpath("com.google.gms:google-services:4.3.13")
        classpath("com.google.firebase:perf-plugin:1.4.1")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.1")

        // Realm Database
        classpath("io.realm:realm-gradle-plugin:10.11.1")

        // Android X Navigation components
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${libs.versions.androidx.navigation.version.get()}")

        // Deployment
        classpath("com.github.triplet.gradle:play-publisher:3.7.0")
        classpath("com.google.firebase:firebase-appdistribution-gradle:3.0.3")

        // Hilt
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.43.1")
    }

}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "SimMatcherGitHubPackages"
            url = uri("https://maven.pkg.github.com/simprints/lib-android-simmatcher")
            credentials {
                username = gradleLocalProperties(rootDir).getProperty("GITHUB_USERNAME")
                    ?: System.getenv("GITHUB_USERNAME")
                password = gradleLocalProperties(rootDir).getProperty("GITHUB_TOKEN")
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        kotlinOptions.freeCompilerArgs += "-Xnew-inference"
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("runAllJacocoTests", GradleBuild::class) {
    group = "verification"
    tasks = listOf(
        "clientapi:jacocoTestReportDebug", "core:jacocoTestReportDebug",
        "id:jacocoTestReportDebug", "face:jacocoTestReportDebug",
        "fingerprint:jacocoTestReportDebug", "fingerprintscanner:jacocoTestReportDebug"
    )
}

plugins {
    id("org.gradle.test-retry") version "1.4.0"
}

/*
Run tests in parallel to speed up tests
https://docs.gradle.org/nightly/userguide/performance.html#suggestions_for_java_projects
*/
tasks.withType(Test::class).configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    retry.maxRetries.set(5)
}
