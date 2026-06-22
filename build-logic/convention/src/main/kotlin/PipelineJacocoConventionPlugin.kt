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

    /**
     * Unified exclusion list used for both class-directory filtering in the JacocoReport task
     * and the JacocoTaskExtension excludes on each Test task.
     */
    private val excludedPatterns = listOf(
        // Android generated
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        // Test classes
        "**/*Test*.*",
        // Android framework
        "android/**/*.*",
        // JDK internals
        "jdk.internal.*",
        // Kotlin-generated classes — Jacoco cannot handle multiple "$" in class names
        "**/*\$\$*",
        "**/*\$Lambda$*.*",
        "**/*\$inlined\$*.*",
        // Dagger / Hilt generated (via KSP)
        "**/*Dagger*.*",
        "**/*MembersInjector*.*",
        "**/*_Provide*Factory*.*",
        "**/*_Factory*.*",
        "**/*_HiltModule*.*",
        "**/*_AssistedFactory*.*",
        "**/*_AssistedFactory_Impl*.*",
        "**/hilt_aggregated_deps/**",
        // Room generated (via KSP) — DAO and Database implementations
        "**/*Dao_Impl*.*",
        "**/*Database_Impl*.*",
        // Other generated code
        "**/*\$JsonObjectMapper.*",
        "**/*\$Icepick.*",
        "**/*\$StateSaver.*",
        "**/*AutoValue_*.*",
    )

    private fun Project.createJacocoTask() {
        tasks.create("jacocoTestReport", JacocoReport::class.java) {
            dependsOn(tasks.withType<Test>().matching { it.name.lowercase().contains("debug") })

            reports.xml.required.set(true)
            reports.html.required.set(false) // Disable html reports to decrease report upload/download time in github pipeline

            val buildDir = layout.buildDirectory.get().asFile

            val compiledClassPaths = listOf(
                "tmp/kotlin-classes/debug",
                "intermediates/javac/debug/classes",
                "intermediates/asm_instrumented_project_classes/debug",
                "intermediates/classes/debug",
                "intermediates/local_classes/debug",
                "intermediates/project_classes/debug",
            )

            val classTrees = compiledClassPaths.map { path ->
                fileTree(buildDir.resolve(path)) {
                    exclude(excludedPatterns)
                }
            }

            classDirectories.setFrom(files(classTrees))
            sourceDirectories.setFrom(
                files(
                    "${project.projectDir}/src/main/java",
                    "${project.projectDir}/src/main/kotlin",
                ),
            )

            executionData.setFrom(
                fileTree(buildDir) {
                    include(
                        "jacoco/testDebugUnitTest.exec", // Standard Gradle / Old AGP
                        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec", // Modern AGP Unit Tests
                        "outputs/code-coverage/connected/*coverage.ec", // Old AGP Instrumented Tests
                        "outputs/code_coverage/debugAndroidTest/connected/**/*.ec", // Modern AGP Instrumented Tests
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
                excludes = excludedPatterns
            }
        }
    }
}
