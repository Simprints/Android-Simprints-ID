import com.android.build.api.dsl.LibraryExtension
import common.androidTestImplementation
import common.getLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class TestingAndroidConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("simprints.android.library")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    testInstrumentationRunnerArguments["clearPackageData"] = "true"
                }
                testOptions {
                    execution = "ANDROIDX_TEST_ORCHESTRATOR"
                    animationsDisabled = true
                }
            }

            val libs = getLibs()

            dependencies {
                add("androidTestUtil", libs.findLibrary("testing.AndroidX.orchestrator").get())

                add("androidTestImplementation", project(":infra:test-tools"))
                androidTestImplementation(libs, "testing.Mockk.android")
                androidTestImplementation(libs, "testing.AndroidX.uiAutomator")
            }
        }
    }
}
