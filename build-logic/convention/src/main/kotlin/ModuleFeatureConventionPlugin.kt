import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class ModuleFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("simprints.android.library")
                apply("simprints.library.hilt")
                apply("simprints.testing.unit")

                apply("androidx.navigation.safeargs.kotlin")
            }
            extensions.configure<LibraryExtension> {
                buildFeatures.viewBinding = true

                testOptions {
                    unitTests.isReturnDefaultValues = true
                    unitTests.isIncludeAndroidResources = true
                    execution = "ANDROIDX_TEST_ORCHESTRATOR"
                    animationsDisabled = true
                }
            }

            dependencies {
                add("implementation", project(":infra:ui-base"))
                add("implementation", project(":infra:core"))
            }
        }
    }
}
