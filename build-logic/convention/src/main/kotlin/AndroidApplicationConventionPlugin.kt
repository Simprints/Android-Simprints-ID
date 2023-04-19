import com.android.build.api.dsl.ApplicationExtension
import common.BuildTypes
import common.SdkVersions
import common.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.withGroovyBuilder

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(from = "${rootDir}/build-logic/build_properties.gradle")
            val props = extra.properties
            val propVersionCode = props["VERSION_CODE"] as Int
            val propVersionName = props["VERSION_NAME"] as String
            val propVersionSuffix = props["VERSION_SUFFIX"] as String
            val propDebuggable = props["DEBUGGABLE"] as Boolean

            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("simprints.library.hilt")

                apply("com.google.firebase.firebase-perf")
                apply("com.google.gms.google-services")
                apply("com.google.firebase.crashlytics")

                apply("simprints.ci.jacoco")
                apply("simprints.ci.sonar")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)

                defaultConfig {
                    targetSdk = SdkVersions.TARGET
                    minSdk = SdkVersions.MIN

                    versionCode = propVersionCode
                    versionName = propVersionName

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    testInstrumentationRunnerArguments["clearPackageData"] = "true"
                }

                testOptions {
                    unitTests.isReturnDefaultValues = true
                    unitTests.isIncludeAndroidResources = true
                    execution = "ANDROIDX_TEST_ORCHESTRATOR"
                    animationsDisabled = true
                }

                bundle.language.enableSplit = false

                buildFeatures {
                    viewBinding = true
                    buildConfig = true
                }

                packaging {
                    // The below files are duplicated from kotlinx-coroutines-debug.
                    // We should exclude them in the packaging options as per kotlinx.coroutines/kotlinx-coroutines-debug documentation
                    // https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-debug#build-failures-due-to-duplicate-resource-files
                    resources.excludes.add("**/attach_hotspot_windows.dll")
                    resources.excludes.add("META-INF/*")
                }

                lint {
                    warning += setOf("InvalidPackage")
                }

                buildTypes {
                    getByName(BuildTypes.release) {
                        isMinifyEnabled = true
                        isDebuggable = false
                        lint.fatal += "StopShip"
                        versionNameSuffix = "+$propVersionCode"
                        buildConfigField("Boolean", "DEBUG_MODE", "false")
                    }

                    create(BuildTypes.staging) {
                        isMinifyEnabled = true
                        isDebuggable = propDebuggable
                        lint.fatal += "StopShip"
                        versionNameSuffix = "-{$propVersionSuffix}+$propVersionCode"
                        buildConfigField("Boolean", "DEBUG_MODE", "true")
                    }

                    getByName(BuildTypes.debug) {
                        isMinifyEnabled = false
                        isDebuggable = propDebuggable
                        versionNameSuffix = "-{$propVersionSuffix}+$propVersionCode"
                        buildConfigField("Boolean", "DEBUG_MODE", "true")

                        withGroovyBuilder {
                            "FirebasePerformance" {
                                invokeMethod("setInstrumentationEnabled", false)
                            }
                        }
                    }
                }
            }
        }
    }
}
