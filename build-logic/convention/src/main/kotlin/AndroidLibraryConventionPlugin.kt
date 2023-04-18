import com.android.build.api.dsl.LibraryExtension
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
                apply("simprints.ci.sonar")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)

                buildFeatures.buildConfig = true
                packaging {
                    // remove mockk duplicated files
                    resources.excludes.addAll(listOf(
                        "META-INF/*",
                    ))
                    resources.pickFirsts += setOf("mockito-extensions/org.mockito.plugins.MockMaker")
                }
            }

            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
            }
        }
    }
}
