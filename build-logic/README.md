# Build logic

SID codebase is split into many small submodules for multiple reasons - to improve build times, to
encapsulate specific technology within the limited context and to improve the overall
comprehensibility of the codebase. Such modularity comes with a trade-off in terms of configuration
overhead for each module, which is why this module exists.

Gradle-way to fight increasing overhead is by creating “convention plugins” to define subsets of
configuration for specific features/functionality that could be applied to project modules in a
composable way.

### Additional reading

* [Blogpost about “Now in Android” implementation of convention plugins](https://proandroiddev.com/exploring-now-in-android-gradle-convention-plugins-91983825bcd7)
* [“Now in Android” that was used as a template](https://github.com/android/nowinandroid)
* [Gradle docs on convention plugins](https://docs.gradle.org/current/samples/sample_convention_plugins.html#compiling_convention_plugins)

## SID convention plugins

* Base Android plugins for application and library modules
    * Define the basic configuration, e.g. Kotlin setup, target SDK version, typical build type
      configuration, etc.
    * Examples: `simprints.android.application`, simprints.android.library`

* Base SID-specific module plugins
    * Define common fundamental dependencies shared by modules of the same type.
    * Examples: `simprints.infra`, `simprints.feature`

* Library configuration plugins
    * Define a configuration for one specific library that requires more than just a dependency
      declaration.
    * Examples: `simprints.android.hilt`, `simprints.library.room` etc

* Pipeline configuration plugins
    * Same as library plugins, but for CI pipeline exclusive plugins/features, e.g. code coverage.

* Testing configuration plugins
    * Define standard testing configurations and dependencies.
    * Examples: `simprints.testing.unit`, `simprints.testing.android`

* Configuration plugins
    * Define some build configurations that could apply in multiple modules.
    * Examples: `simprints.config.cloud`, `simprints.config.cloud`
    * Note: this is a sub-optimal solution and is subject to change.

## Types of modules in SID

* Application module (`:id`)
    * The main application launcher module. The only one that uses the application plugin and
      depends on other module types.

* Feature modules (prefixed with `:feature_`
    * Implement parts of the application user flow and define some UI elements.
    * Use `simprints.feature` convention plugin that includes:
        * Base Android library configuration
        * Dependency on core modules
        * Hilt configuration
        * Fundamental UI-related dependencies, including navigation
        * Standard unit-testing configuration

* Infra modules (prefixed with `:infra_`)
    * Implement an encapsulated part of application business logic.
    * Use `simprints.infra` convention plugin that includes:
        * Base Android library configuration
        * Dependency on core modules
        * Hilt configuration
        * Standard unit-testing configuration

* Core modules
    * Implement some fundamental shared solution (e.g. networking, logging) or utility code (`:core`
      module itself).
    * Use `simprints.android.library` as the base and explicitly define all the required
      dependencies and configuration.
    * `:core` module transitively exposes all other core modules and a small set of fundamental
      dependencies (subject to clarification and refinement in future)

## How to add a new module

1. Decide on the type of module (feature, infra or core).
2. Create a module in the codebase with the appropriate naming convention.
3. In the module’s `build.gradle.kts`
    1. Replace `com.android.library` plugin with the proper convention plugin. Apply required
       library
       plugins if necessary.
    2. Remove everything except `namespace` and module-specific configuration from the `android`
       block.
    3. Remove everything except module-specific dependency definitions.
4. In projects `settings.gradle.kts` move new module's definition to appropriate `include` block.

## Adding 3rd party plugin to the project

Due to the unorthodox setup with convention plugins adding 3rd party plugins requires a bit more
setting up than usual:

* Add required definitions to `libs.versions.toml` - version, plugin’s classpath definition
  to `libs` section and plugin id alias to `plugins` section
* Add plugin dependency to `build-logic/convention/build.gradle.kts` (similar to `classpath`
  definition in traditional setup)
* Add plugin alias in the root `build.gradle.kts` (required to discover the plugin in convention
  plugin implementations)
* Create a library convention plugin that applies the new 3rd party plugin and provides the required
  configuration. Register the new convention plugin in `build-logic/convention/build.gradle.kts`.
* Apply the new convention plugin in the appropriate module or base convention plugin. 
