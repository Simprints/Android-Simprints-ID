import common.getLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class PipelineJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.gradle.jacoco")
            }
            createJacocoTask()
            configureJacoco()
        }
    }

    private val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )

    private fun Project.createJacocoTask() {
        tasks.create("jacocoTestReport", JacocoReport::class.java) {
            dependsOn(tasks.withType<Test>().matching { it.name.lowercase().contains("debug") })

            reports.xml.required.set(true)
            reports.html.required.set(false) // Disable html reports to decrease report upload/download time in github pipeline

            val javaTree = fileTree("${project.buildDir}/intermediates/javac/debug/classes") { exclude(fileFilter) }
            val kotlinTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") { exclude(fileFilter) }
            classDirectories.setFrom(files(javaTree, kotlinTree))

            sourceDirectories.setFrom(files("${project.projectDir}/src/main/java"))

            executionData.setFrom(
                fileTree("$buildDir") {
                    include(
                        "jacoco/testDebugUnitTest.exec",
                        "outputs/code-coverage/connected/*coverage.ec",
                    )
                },
            )
        }
    }

    private fun Project.configureJacoco() {
        extensions.configure<JacocoPluginExtension> {
            toolVersion = getLibs().findVersion("jacoco.version").get().toString()
        }

        tasks.withType<Test> {
            extensions.configure<JacocoTaskExtension> {
                isIncludeNoLocationClasses = true
                excludes = listOf(
                    "jdk.internal.*",
                    "**/R.class",
                    "**/R$*.class",
                    "**/*$$*",
                    "**/BuildConfig.*",
                    "**/Manifest*.*",
                    "**/*\$Lambda$*.*", // Jacoco can not handle several "$" in class name.
                    "**/*Dagger*.*", // Dagger auto-generated code.
                    "**/*MembersInjector*.*", // Dagger auto-generated code.
                    "**/*_Provide*Factory*.*", // Dagger auto-generated code.
                    "**/*_Factory*.*", // Dagger auto-generated code.
                    "**/*\$JsonObjectMapper.*", // LoganSquare auto-generated code.
                    "**/*\$inlined$*.*", // Kotlin specific, Jacoco can not handle several "$" in class name.
                    "**/*\$Icepick.*", // Icepick auto-generated code.
                    "**/*\$StateSaver.*", // android-state auto-generated code.
                    "**/*AutoValue_*.*", // AutoValue auto-generated code.
                )
            }
        }
    }
}
