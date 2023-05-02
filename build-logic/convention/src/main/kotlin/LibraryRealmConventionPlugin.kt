import common.configureDbEncryptionBuild
import org.gradle.api.Plugin
import org.gradle.api.Project

class LibraryRealmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.kapt")
                apply("realm-android")
            }

            configureDbEncryptionBuild()
        }
    }
}
