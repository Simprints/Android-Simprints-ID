import common.api
import common.configureDbEncryptionBuild
import common.getLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class LibraryRealmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.simprints.realm.kotlin")
            }

            configureDbEncryptionBuild()

            val libs = getLibs()
            dependencies {
                api(libs, "realm.base")
            }
        }
    }
}
