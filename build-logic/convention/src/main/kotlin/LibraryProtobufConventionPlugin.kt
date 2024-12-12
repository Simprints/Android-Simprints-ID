import com.android.build.api.variant.AndroidComponentsExtension
import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufExtension
import common.getLibs
import common.implementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool

class LibraryProtobufConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.protobuf")
            }

            val libs = getLibs()

            extensions.configure<ProtobufExtension> {
                protoc {
                    artifact = "com.google.protobuf:protoc:${libs.findVersion("protobuf.version").get()}"
                }

                generateProtoTasks {
                    all().forEach { task ->
                        task.builtins { create("java") { option("lite") } }
                    }
                }
            }

            dependencies {
                implementation(libs, "protobuf")
            }

            // Fox for known issue with KSP and protobuf
            // https://github.com/google/ksp/issues/1590#issuecomment-1826387452
            project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
                afterEvaluate {
                    val variantName = variant.name.replaceFirstChar { it.uppercaseChar() }
                    val generateTaskName = "generate${variantName}Proto"
                    val kspTaskName = "ksp${variantName}Kotlin"

                    val protoTask = project.tasks.getByName(generateTaskName) as GenerateProtoTask
                    project.tasks.getByName(kspTaskName) {
                        dependsOn(protoTask)
                        (this as AbstractKotlinCompileTool<*>).setSource(protoTask.outputBaseDir)
                    }
                }
            }
        }
    }
}
