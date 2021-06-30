plugins {
    id("com.android.library")
    id("com.jfrog.artifactory")
    id("kotlin-android")
    id("maven-publish")
}

apply {
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests {
            all {
                it.testLogging {
                    events("started", "passed", "skipped", "failed")
                }
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    implementation(Dependencies.Kotlin.reflect)
    compileOnly(Dependencies.AndroidX.Annotation.annotation)

    // Logging
    implementation(project(":logging"))

    // RxJava
    api(Dependencies.RxJava2.core)
    implementation(Dependencies.RxJava2.kotlin)

    // ######################################################
    //                      Unit test
    // ######################################################

    // Simprints
    testImplementation(project(":testtools"))

    // JUnit
    testImplementation(Dependencies.Testing.junit)

    // Mockito
    testImplementation(Dependencies.Testing.Mockito.kotlin)
    testImplementation(Dependencies.Testing.Mockito.core)
    testImplementation(Dependencies.Testing.Mockito.inline)

    // Truth
    testImplementation(Dependencies.Testing.truth)
}
