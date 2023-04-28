import com.android.build.api.dsl.LibraryExtension
import common.configureDbEncryptionBuild
import common.getLibs
import common.implementation
import common.kapt
import common.testImplementation
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies


class LibraryRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.kapt")
            }

            configureDbEncryptionBuild()

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    javaCompileOptions {
                        annotationProcessorOptions {
                            //Required by Room to be able to export the db schemas
                            arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
                        }
                    }
                }

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
                kapt(libs, "androidX.Room.compiler")

                implementation(libs, "sqlCipher.core")

                testImplementation(libs, "testing.AndroidX.room")
            }
        }
    }

}
