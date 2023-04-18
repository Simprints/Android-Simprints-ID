import common.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class LibraryHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("dagger.hilt.android.plugin")
                // KAPT must go last to avoid build warnings.
                // See: https://stackoverflow.com/questions/70550883/warning-the-following-options-were-not-recognized-by-any-processor-dagger-f
                apply("org.jetbrains.kotlin.kapt")
            }

            extensions.configure<KaptExtension> {
                useBuildCache = true
                arguments {
                    arg("realm.ignoreKotlinNullability", true)
                }
            }

            val libs = getLibs()
            dependencies {
                implementation(libs, "hilt")
                implementation(libs, "hilt.work") // TODO move to worker config?

                kapt(libs, "hilt.kapt")
                kapt(libs, "hilt.compiler")

                kaptTest(libs, "testing.hilt.kapt")
                kaptAndroidTest(libs, "testing.hilt.kapt")
            }
        }
    }

}
