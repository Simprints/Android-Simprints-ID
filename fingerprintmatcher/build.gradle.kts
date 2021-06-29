plugins {
    id("com.jfrog.artifactory")
    id("maven-publish")
    id("com.android.library")
    id("kotlin-android")
}

apply {
    from("profiling.gradle")
    from("${rootDir}${File.separator}buildSrc${File.separator}build_config.gradle")
}

android {

    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        getByName(BuildParams.BuildTypes.profiling) {
            initWith(getByName(BuildParams.BuildTypes.debug))
        }
    }

    externalNativeBuild {
        cmake.path("src/main/jni/CMakeLists.txt")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    implementation(Dependencies.Kotlin.coroutines_android)
    compileOnly(Dependencies.AndroidX.Annotation.annotation)

    testImplementation(Dependencies.Testing.junit)
}
