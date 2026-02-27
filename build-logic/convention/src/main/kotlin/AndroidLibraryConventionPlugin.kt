import com.android.build.api.dsl.LibraryExtension
import common.BuildTypes
import common.configureAndroidLibrary
import common.configureDebugModeBuildTypes
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

                apply("simprints.ci.jacoco")
            }

            extensions.configure<LibraryExtension> {
                configureAndroidLibrary(this)

                packaging {
                    // remove mockk duplicated files
                    resources.excludes.addAll(
                        listOf(
                            "META-INF/*",
                        ),
                    )
                }

                buildFeatures.buildConfig = true
                buildTypes {
                    // In a library module, we generally don’t need to
                    // add any specific configurations here because the app module handles shrinking,
                    // obfuscation, and signing. Leaving this block empty means the default behavior is inherited.
                    getByName(BuildTypes.RELEASE) {
                    }

                    create(BuildTypes.STAGING) {
                    }

                    getByName(BuildTypes.DEBUG) {
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
