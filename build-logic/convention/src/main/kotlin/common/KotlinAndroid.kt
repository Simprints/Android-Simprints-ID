package common

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinCompiler() {
    tasks.withType(KotlinCompile::class.java).configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xnew-inference",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.Experimental",
            )
        }
    }
}

internal fun Project.configureAndroidApplication(extension: ApplicationExtension) {
    extension.apply {
        compileSdk = SdkVersions.TARGET

        defaultConfig {
            minSdk = SdkVersions.MIN
        }

        compileOptions {
            sourceCompatibility = SdkVersions.JAVA_TARGET
            targetCompatibility = SdkVersions.JAVA_TARGET
        }
    }

    configureKotlinCompiler()
}

internal fun Project.configureAndroidLibrary(extension: LibraryExtension) {
    extension.apply {
        compileSdk = SdkVersions.TARGET

        defaultConfig {
            minSdk = SdkVersions.MIN
        }

        compileOptions {
            sourceCompatibility = SdkVersions.JAVA_TARGET
            targetCompatibility = SdkVersions.JAVA_TARGET
        }
    }

    configureKotlinCompiler()
}
