import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class ModuleInfraConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("simprints.android.library")
                apply("simprints.library.hilt")
                apply("simprints.testing.unit")
            }
            extensions.configure<LibraryExtension> {
                defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                testOptions {
                    unitTests.isReturnDefaultValues = true
                }
            }

            dependencies {
                add("implementation", project(":infra:core"))
            }
        }
    }
}
