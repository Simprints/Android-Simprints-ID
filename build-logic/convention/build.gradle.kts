plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            // Due to bug either in AS or explicit fragment dependency does not override transitive
            // dependencies and it is not possible to use newest features of both FragmentActivity
            // and AppCompatActivity (we use both for screen results API).
            // https://issuetracker.google.com/u/0/issues/178403178#comment17
            substitute(module("androidx.fragment:fragment:1.0.0"))
                .using(module("androidx.fragment:fragment:${libs.versions.androidx.fragment.version.get()}"))
        }
    }
}

// TODO root/build.gradle same as buildscript.dependencies{}
dependencies {
    // Gradle & Kotlin
    compileOnly(libs.plugin.gradle.android)
    compileOnly(libs.plugin.gradle.kotlin)

    // Firebase
    compileOnly(libs.plugin.gms)
    compileOnly(libs.plugin.firebase.crashlytics)
    compileOnly(libs.plugin.firebase.perf)
    // Protobuf
    compileOnly(libs.plugin.protobuf)
    // Hilt
    compileOnly(libs.plugin.hilt)
    // Android X Navigation components
    compileOnly(libs.plugin.navigation)
    // Deployment
    compileOnly(libs.plugin.play.publisher)
    compileOnly(libs.plugin.firebase.distribution)

    // CI Scanning & Retry
    compileOnly(libs.plugin.sonar)
    compileOnly(libs.plugin.jacoco)
    compileOnly(libs.plugin.depsAnalysis)
    compileOnly(libs.plugin.retry)

    // TODO Uncomment when issue in root build.gradle.kts is solved
    // Realm Database
    // compileOnly(libs.plugin.realm)
}

gradlePlugin {
    plugins {
        // General setup for the base android module types
        register("androidApplication") {
            id = "simprints.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "simprints.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        // CI related plugin configuration
        register("pipelineJacoco") {
            id = "simprints.ci.jacoco"
            implementationClass = "PipelineJacocoConventionPlugin"
        }
        register("pipelineSonar") {
            id = "simprints.ci.sonar"
            implementationClass = "PipelineSonarConventionPlugin"
        }

        // Utility plugins for easy build config setup in module
        register("configCloud") {
            id = "simprints.config.cloud"
            implementationClass = "ConfigCloudConventionPlugin"
        }
        register("configNetwork") {
            id = "simprints.config.network"
            implementationClass = "ConfigNetworkConventionPlugin"
        }

        // Utility plugins for specific library configuration in individual modules
        register("libraryHilt") {
            id = "simprints.library.hilt"
            implementationClass = "LibraryHiltConventionPlugin"
        }
        register("libraryRealm") {
            id = "simprints.library.realm"
            implementationClass = "LibraryRealmConventionPlugin"
        }
        register("libraryRoom") {
            id = "simprints.library.room"
            implementationClass = "LibraryRoomConventionPlugin"
        }
        register("libraryProtobuf") {
            id = "simprints.library.protobuf"
            implementationClass = "LibraryProtobufConventionPlugin"
        }

        // Testing setup plugins
        register("testingUnit") {
            id = "simprints.testing.unit"
            implementationClass = "TestingUnitConventionPlugin"
        }
        register("testingAndroid") {
            id = "simprints.testing.android"
            implementationClass = "TestingAndroidConventionPlugin"
        }

        // Generally all project modules must use one of these plugins as a base
        register("moduleFeature") {
            id = "simprints.feature"
            implementationClass = "ModuleFeatureConventionPlugin"
        }
        register("moduleInfra") {
            id = "simprints.infra"
            implementationClass = "ModuleInfraConventionPlugin"
        }
    }
}
