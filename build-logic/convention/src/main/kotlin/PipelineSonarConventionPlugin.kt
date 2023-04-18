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
                        property("sonar.sources", "$projectDir$sourceDir")
                        property("sonar.java.binaries", "$projectDir$binariesDir")
                        property("sonar.coverage.jacoco.xmlReportPaths", listOf("$projectDir$jacocoDir"))
                    }
                }
            }

            /*
             * We skip the infraresources module because it has no source code to analyse. This should be
             * removed if that ever changes
             */
            project(":infraresources") {
                extensions.configure<SonarExtension> {
                    isSkipProject = true
                }
            }
        }
    }

    companion object {
        private val jacocoDir = "${File.separator}build${File.separator}reports${File.separator}jacoco${File.separator}jacocoTestReport${File.separator}jacocoTestReport.xml"
        private val sourceDir = "${File.separator}src${File.separator}main${File.separator}java${File.separator}com${File.separator}simprints"
        private val binariesDir = "${File.separator}build${File.separator}intermediates${File.separator}**"
    }
}
