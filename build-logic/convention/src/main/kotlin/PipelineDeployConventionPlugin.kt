import com.android.build.api.dsl.ApplicationExtension
import com.github.triplet.gradle.play.PlayPublisherExtension
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

class PipelineDeployConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(from = "${rootDir}/build-logic/signing_properties.gradle.kts")
            val props = extra.properties

            with(pluginManager) {
                apply("com.github.triplet.play")
                apply("com.google.firebase.appdistribution")
            }

            extensions.configure<PlayPublisherExtension> {
                serviceAccountCredentials.set(file("$rootDir/google_api_key.json"))
                artifactDir.set(file("build/outputs/bundle/release"))
            }

            extensions.configure<ApplicationExtension> {
                signingConfigs {
                    create("config") {
                        keyAlias = props["key_alias"] as String
                        keyPassword = props["key_password"] as String
                        storeFile = file(props["store_file"] as String)
                        storePassword = props["store_password"] as String
                    }
                    create("stagingConfig") {
                        keyAlias = props["staging_key_alias"] as String
                        keyPassword = props["key_password"] as String
                        storeFile = file(props["store_file"] as String)
                        storePassword = props["store_password"] as String
                    }
                }

                buildFeatures.buildConfig = true
                buildTypes {
                    getByName("release") {
                        signingConfig = signingConfigs.getByName("config")
                    }
                    getByName("staging") {
                        signingConfig = signingConfigs.getByName("stagingConfig")

                        firebaseAppDistribution {
                            appId = "1:423867324644:android:7c4e84b7f391800f44b4f1"
                            artifactType = "APK"
                            artifactPath = "$rootDir/id/build/outputs/apk_from_bundle/staging/id-staging-universal.apk"
                            serviceCredentialsFile = "id/src/staging/serviceCredentialsFile.json"
                            groups = "pre-release-testers"
                        }
                    }
                    getByName("debug") {
                        signingConfig = signingConfigs.getByName("config")
                    }
                }
            }
        }
    }
}
