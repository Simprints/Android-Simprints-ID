package common

import com.android.build.api.dsl.LibraryBuildType
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.konan.file.File

fun NamedDomainObjectContainer<LibraryBuildType>.configureDebugModeBuildTypes() {
    getByName(BuildTypes.RELEASE) {
        buildConfigField("Boolean", "DEBUG_MODE", "false")
    }
    getByName(BuildTypes.STAGING) {
        buildConfigField("Boolean", "DEBUG_MODE", "true")
    }
    getByName(BuildTypes.DEBUG) {
        buildConfigField("Boolean", "DEBUG_MODE", "true")
    }
}

fun Project.configureDbEncryptionBuild() {
    apply(from = "${rootDir}${File.separator}build-logic${File.separator}build_properties.gradle.kts")
    val props = extra.properties
    val propDbEncrypted = props["DB_ENCRYPTION"] as Boolean

    extensions.configure<LibraryExtension> {
        buildTypes {
            getByName(BuildTypes.RELEASE) {
                buildConfigField("Boolean", "DB_ENCRYPTION", "$propDbEncrypted")
            }

            getByName(BuildTypes.STAGING) {
                buildConfigField("Boolean", "DB_ENCRYPTION", "$propDbEncrypted")
            }

            getByName(BuildTypes.DEBUG) {
                buildConfigField("Boolean", "DB_ENCRYPTION", "$propDbEncrypted")
            }
        }
    }
}

fun NamedDomainObjectContainer<LibraryBuildType>.configureCloudAccessBuildTypes() {
    getByName(BuildTypes.RELEASE) {
        buildConfigField("String", "CLOUD_PROJECT_ID", CloudParams.RELEASE_CLOUD_PROJECT_ID)
    }
    getByName(BuildTypes.STAGING) {
        buildConfigField("String", "CLOUD_PROJECT_ID", CloudParams.STAGING_CLOUD_PROJECT_ID)
    }
    getByName(BuildTypes.DEBUG) {
        buildConfigField("String", "CLOUD_PROJECT_ID", CloudParams.DEV_CLOUD_PROJECT_ID)
    }
}

fun NamedDomainObjectContainer<LibraryBuildType>.configureNetworkBuildTypes() {
    getByName(BuildTypes.RELEASE) {
        buildConfigField("String", "BASE_URL_PREFIX", "\"prod\"")
    }
    getByName(BuildTypes.STAGING) {
        buildConfigField("String", "BASE_URL_PREFIX", "\"staging\"")
    }
    getByName(BuildTypes.DEBUG) {
        buildConfigField("String", "BASE_URL_PREFIX", "\"dev\"")
    }
}
