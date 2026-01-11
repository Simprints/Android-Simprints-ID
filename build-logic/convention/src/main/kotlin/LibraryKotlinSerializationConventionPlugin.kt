import common.configureDbEncryptionBuild
import common.getLibs
import common.implementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class LibraryKotlinSerializationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureDbEncryptionBuild()

            val libs = getLibs()
            dependencies {
                implementation(libs, "kotlin.serialization")
            }
        }
    }
}
