plugins {
    `kotlin-dsl`
}

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
    // Room
    compileOnly(libs.plugin.room)
    // Hilt
    compileOnly(libs.plugin.hilt)
    // Android X Navigation components
    compileOnly(libs.plugin.navigation)
    // Deployment
    compileOnly(libs.plugin.play.publisher)

    // CI Scanning & Retry
    compileOnly(libs.plugin.sonar)
    compileOnly(libs.plugin.jacoco)
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
        register("pipelineDeploy") {
            id = "simprints.ci.deploy"
            implementationClass = "PipelineDeployConventionPlugin"
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
