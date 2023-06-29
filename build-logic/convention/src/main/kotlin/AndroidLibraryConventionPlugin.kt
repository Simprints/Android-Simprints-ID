import com.android.build.api.dsl.LibraryExtension
import common.BuildTypes
import common.configureDebugModeBuildTypes
import common.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")

                apply("simprints.ci.jacoco")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                packaging {
                    // remove mockk duplicated files
                    resources.excludes.addAll(listOf(
                        "META-INF/*",
                    ))
                    resources.pickFirsts += setOf("mockito-extensions/org.mockito.plugins.MockMaker")
                }

                buildFeatures.buildConfig = true
                buildTypes {
                    getByName(BuildTypes.release) {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "$rootDir/build-logic/proguard-rules.pro"
                        )
                    }
                    create(BuildTypes.staging) {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "$rootDir/build-logic/proguard-rules.pro"
                        )
                    }
                    getByName(BuildTypes.debug) {
                        isMinifyEnabled = false
                    }
                    configureDebugModeBuildTypes()
                }
            }

            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
            }
        }
    }
}
