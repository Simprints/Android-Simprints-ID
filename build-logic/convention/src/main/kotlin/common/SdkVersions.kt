package common

import org.gradle.api.JavaVersion

object SdkVersions {
    const val MIN = 24
    const val COMPILE = 37
    const val TARGET = 37

    val JAVA_TARGET = JavaVersion.VERSION_21
}
