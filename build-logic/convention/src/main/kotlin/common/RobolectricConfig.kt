package common

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import java.io.File

/**
 * Injects a default robolectric.properties into the test task classpath if the module doesn't
 * provide its own. This prevents Robolectric from failing when targetSdkVersion exceeds its max
 * supported SDK. Module-specific files in src/test/resources/ always take priority.
 */
fun addDefaultRobolectricConfig(project: Project, testTask: Test) {
    if (project.file("src/test/resources/robolectric.properties").exists()) return

    val generatedDir = project.layout.buildDirectory.dir("generated/robolectric")
    testTask.doFirst {
        val dir = generatedDir.get().asFile
        dir.mkdirs()
        File(dir, "robolectric.properties").apply {
            if (!exists()) writeText("sdk=28\ninstrumentedPackages=androidx.loader.content\n")
        }
    }
    testTask.classpath = project.files(generatedDir) + testTask.classpath
}
