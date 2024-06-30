import com.android.build.api.dsl.LibraryExtension
import common.configureDbEncryptionBuild
import common.getLibs
import common.implementation
import common.ksp
import common.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import androidx.room.gradle.RoomExtension

class LibraryRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("androidx.room")
                apply("com.google.devtools.ksp")
            }

            configureDbEncryptionBuild()

            extensions.configure<RoomExtension> {
                //Required by Room to be able to export the db schemas
                schemaDirectory("$projectDir/schemas")
            }

            extensions.configure<LibraryExtension> {
                sourceSets {
                    getByName("debug") {
                        assets.srcDirs("$projectDir/schemas")
                    }
                    getByName("test") {
                        java.srcDirs("$projectDir/src/debug")
                    }
                }
            }

            val libs = getLibs()
            dependencies {
                implementation(libs, "androidX.Room.core")
                implementation(libs, "androidX.Room.ktx")
                ksp(libs, "androidX.Room.compiler")

                implementation(libs, "sqlCipher.core")

                testImplementation(libs, "testing.AndroidX.room")
            }
        }
    }

}
