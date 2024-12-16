import common.getLibs
import common.implementation
import common.ksp
import common.kspAndroidTest
import common.kspTest
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class LibraryHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("dagger.hilt.android.plugin")
                apply("com.google.devtools.ksp")
            }

            val libs = getLibs()
            dependencies {
                implementation(libs, "hilt")
                implementation(libs, "hilt.work")

                ksp(libs, "hilt.dagger.compiler")
                ksp(libs, "hilt.compiler")

                kspTest(libs, "testing.hilt.compiler")
                kspAndroidTest(libs, "testing.hilt.compiler")
            }
        }
    }
}
