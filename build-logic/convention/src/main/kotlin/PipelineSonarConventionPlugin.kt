import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.sonarqube.gradle.SonarExtension
import java.io.File

class PipelineSonarConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.sonarqube")
            }

            extensions.configure<SonarExtension> {
                properties {
                    property("sonar.projectName", "Simprints ID")
                    property("sonar.c.file.suffixes", "-")
                    property("sonar.java.coveragePlugin", "jacoco")
                }
            }

            subprojects {
                extensions.configure<SonarExtension> {
                    properties {
                        // Fix for https://community.sonarsource.com/t/random-sub-projects-fail-analysis-in-gradle-multi-projects-build/49777
                        property("sonar.userHome", "$projectDir$cacheDir")
                        property("sonar.sources", "$projectDir$sourceDir")
                        property("sonar.java.binaries", "$projectDir$binariesDir")
                        property("sonar.coverage.jacoco.xmlReportPaths", listOf("$projectDir$jacocoDir"))
                    }
                }
            }
        }
    }

    companion object {
        private val cacheDir = "${File.separator}build${File.separator}.sonar"
        private val jacocoDir = "${File.separator}build${File.separator}reports${File.separator}jacoco${File.separator}jacocoTestReport${File.separator}jacocoTestReport.xml"
        private val sourceDir = "${File.separator}src${File.separator}main${File.separator}java${File.separator}com${File.separator}simprints"
        private val binariesDir = "${File.separator}build${File.separator}"
    }
}
