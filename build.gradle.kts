import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

apply {
    from("ci${File.separator}scanning${File.separator}sonarqube.gradle")
    from("ci${File.separator}scanning${File.separator}dependency_health.gradle")
}

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://storage.googleapis.com/r8-releases/raw/master")
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        // Gradle & Kotlin
        classpath("com.android.tools.build:gradle:7.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Dependencies.kotlin_version}")

        // CI Scanning & Retry
        classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.3")

        classpath("org.jacoco:org.jacoco.core:${Plugins.jacoco}")

        classpath("org.ow2.asm:asm:9.2")
        classpath("com.autonomousapps:dependency-analysis-gradle-plugin:0.78.0")
        classpath("org.gradle:test-retry-gradle-plugin:1.3.1")

        // Firebase
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:perf-plugin:1.4.1")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")

        // Dependency Publishing
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.1")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4.24.18")

        // Realm Database
        classpath("io.realm:realm-gradle-plugin:10.8.0")

        // Android X Navigation components
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Dependencies.androidx_navigation_version}")

        // Deployment
        classpath("com.github.triplet.gradle:play-publisher:3.6.0")
        classpath("com.google.firebase:firebase-appdistribution-gradle:2.2.0")
    }

}

allprojects {
    repositories {
        google()
        jcenter()
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

    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).forEach {
        it.kotlinOptions {
            freeCompilerArgs = listOf("-Xnew-inference")
        }
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
    id("org.gradle.test-retry") version "1.2.0"
}

/*
Run tests in parallel to speed up tests
https://docs.gradle.org/nightly/userguide/performance.html#suggestions_for_java_projects
*/
tasks.withType(Test::class).configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2) ?: 1
    retry.maxRetries.set(5)
}
