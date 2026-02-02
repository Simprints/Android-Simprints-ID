import common.getLibs
import common.implementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class LibraryBackendConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = getLibs()
            dependencies {
                implementation(libs, "retrofit.core")

                add("implementation", project(":infra:backend-api"))
            }
        }
    }
}
