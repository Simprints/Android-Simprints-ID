import com.android.build.api.dsl.LibraryExtension
import common.configureNetworkBuildTypes
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ConfigNetworkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                buildTypes {
                    configureNetworkBuildTypes()
                }
            }
        }
    }
}
