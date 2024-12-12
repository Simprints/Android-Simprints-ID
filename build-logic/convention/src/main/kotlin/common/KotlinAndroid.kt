package common

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = SdkVersions.TARGET

        defaultConfig {
            minSdk = SdkVersions.MIN
        }

        compileOptions {
            sourceCompatibility = SdkVersions.JAVA_TARGET
            targetCompatibility = SdkVersions.JAVA_TARGET
        }

        extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
            afterEvaluate {
                val variantName = variant.name.replaceFirstChar { it.uppercaseChar() }
                val compileTaskName = "compile${variantName}Kotlin"

                tasks.named(compileTaskName, KotlinCompilationTask::class.java) {
                    compilerOptions.freeCompilerArgs.addAll(
                        "-Xnew-inference",
                        "-opt-in=kotlin.RequiresOptIn",
                        // Enable experimental coroutines APIs, including Flow
                        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                        "-opt-in=kotlinx.coroutines.FlowPreview",
                        "-opt-in=kotlin.Experimental",
                    )
                }
            }
        }
    }
}
