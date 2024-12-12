package common

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.getByType

internal fun Project.getLibs() = extensions.getByType<VersionCatalogsExtension>().named("libs")

// TODO Use context receivers  when it is out to make VersionCatalog
//  the actual receiver in DependencyHandlerScope context
internal fun DependencyHandlerScope.implementation(
    libs: VersionCatalog,
    name: String,
) {
    add("implementation", libs.findLibrary(name).get())
}

internal fun DependencyHandlerScope.api(
    libs: VersionCatalog,
    name: String,
) {
    add("api", libs.findLibrary(name).get())
}

internal fun DependencyHandlerScope.testImplementation(
    libs: VersionCatalog,
    name: String,
) {
    add("testImplementation", libs.findLibrary(name).get())
}

internal fun DependencyHandlerScope.androidTestImplementation(
    libs: VersionCatalog,
    name: String,
) {
    add("androidTestImplementation", libs.findLibrary(name).get())
}

internal fun DependencyHandlerScope.ksp(
    libs: VersionCatalog,
    name: String,
) {
    add("ksp", libs.findLibrary(name).get())
}

internal fun DependencyHandlerScope.kspTest(
    libs: VersionCatalog,
    name: String,
) {
    add("kspTest", libs.findLibrary(name).get())
}

internal fun DependencyHandlerScope.kspAndroidTest(
    libs: VersionCatalog,
    name: String,
) {
    add("kspAndroidTest", libs.findLibrary(name).get())
}
