package common

import com.android.build.api.dsl.LibraryBuildType
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

fun NamedDomainObjectContainer<LibraryBuildType>.configureDebugModeBuildTypes() {
    getByName(BuildTypes.release) {
        buildConfigField("Boolean", "DEBUG_MODE", "false")
    }
    getByName(BuildTypes.staging) {
        buildConfigField("Boolean", "DEBUG_MODE", "true")
    }
    getByName(BuildTypes.debug) {
        buildConfigField("Boolean", "DEBUG_MODE", "true")
    }
}

fun Project.configureDbEncryptionBuild() {
    apply(from = "${rootDir}/build-logic/build_properties.gradle")
    val props = extra.properties
    val propDbEncrypted = props["DB_ENCRYPTION"] as Boolean

    extensions.configure<LibraryExtension> {
        buildTypes {
            getByName(BuildTypes.release) {
                buildConfigField("Boolean", "DB_ENCRYPTION", "$propDbEncrypted")
            }

            getByName(BuildTypes.staging) {
                buildConfigField("Boolean", "DB_ENCRYPTION", "$propDbEncrypted")
            }

            getByName(BuildTypes.debug) {
                buildConfigField("Boolean", "DB_ENCRYPTION", "$propDbEncrypted")
            }
        }
    }
}

fun NamedDomainObjectContainer<LibraryBuildType>.configureCloudAccessBuildTypes() {
    getByName(BuildTypes.release) {
        buildConfigField("String", "CLOUD_PROJECT_ID", CloudParams.RELEASE_CLOUD_PROJECT_ID)
    }
    getByName(BuildTypes.staging) {
        buildConfigField("String", "CLOUD_PROJECT_ID", CloudParams.STAGING_CLOUD_PROJECT_ID)
    }
    getByName(BuildTypes.debug) {
        buildConfigField("String", "CLOUD_PROJECT_ID", CloudParams.DEV_CLOUD_PROJECT_ID)
    }
}

fun NamedDomainObjectContainer<LibraryBuildType>.configureNetworkBuildTypes() {
    getByName(BuildTypes.release) {
        buildConfigField("String", "BASE_URL_PREFIX", "\"prod\"")
    }
    getByName(BuildTypes.staging) {
        buildConfigField("String", "BASE_URL_PREFIX", "\"staging\"")
    }
    getByName(BuildTypes.debug) {
        buildConfigField("String", "BASE_URL_PREFIX", "\"dev\"")
    }
}
