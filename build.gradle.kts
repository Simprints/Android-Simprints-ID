import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

apply {
    from("ci${File.separator}scanning${File.separator}sonarqube.gradle")
    from("ci${File.separator}scanning${File.separator}dependency_health.gradle")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        // Gradle & Kotlin
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.version.get()}")

        // CI Scanning & Retry
        classpath("org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:3.5.0.2730")

        classpath("org.jacoco:org.jacoco.core:${libs.versions.jacoco.version.get()}")

        classpath("com.autonomousapps:dependency-analysis-gradle-plugin:1.17.0")
        classpath("org.gradle:test-retry-gradle-plugin:1.5.0")

        // Firebase
        classpath("com.google.gms:google-services:4.3.14")
        classpath("com.google.firebase:perf-plugin:1.4.2")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.2")

        // Realm Database
        classpath("io.realm:realm-gradle-plugin:10.12.0")

        // Android X Navigation components
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${libs.versions.androidx.navigation.version.get()}")

        // Deployment
        classpath("com.github.triplet.gradle:play-publisher:3.7.0")
        classpath("com.google.firebase:firebase-appdistribution-gradle:3.1.1")

        // Hilt
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.44.2")
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

plugins {
    id("org.gradle.test-retry") version "1.5.0"
}

/*
Run tests in parallel to speed up tests
https://docs.gradle.org/nightly/userguide/performance.html#suggestions_for_java_projects
*/
tasks.withType(Test::class).configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
    retry.maxRetries.set(5)
}
