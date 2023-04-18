import com.android.build.api.dsl.LibraryExtension
import common.getLibs
import common.implementation
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

            val libs = getLibs()

            dependencies {
                add("implementation", project(":core"))
                add("testImplementation", project(":testtools"))
                add("androidTestImplementation", project(":testtools"))

                implementation(libs, "androidX.core")
                implementation(libs, "androidX.appcompat")
                implementation(libs, "androidX.lifecycle.scope")

                implementation(libs, "support.material")

                implementation(libs, "androidX.uI.fragment.kotlin")
                implementation(libs, "androidX.uI.constraintlayout")
                implementation(libs, "androidX.uI.coordinatorlayout")
                implementation(libs, "androidX.uI.cardview")

                implementation(libs, "androidX.navigation.fragment")

            }
        }
    }
}
