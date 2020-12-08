# Android-Simprints-ID

## Cloning

There are submodules in this repository [LibSimprints](https://github.com/Simprints/LibSimprints) and [SimMatcher](https://github.com/Simprints/Fingerprint-SimMatcher), so recursive cloning is necessary:

`git clone --recursive git@bitbucket.org:simprints/android-simprints-id.git`

### Full CI Workflow
The aim of the `ci` workflow is to run all tests in all modules, assemble production and debug builds of the APK, and report to the main CI Slack channel.
When run, it immediately triggers all the other relevant workflows such that all tests are run.
In the mean-time, the `ci` build awaits for the other workflows to finish and, if they pass, continue to the assembly and deploy steps.

It is triggered upon pull requests and serves as validation of the integrity of the branch for any pull requests into `develop`.

## Testing

### How to run tests
Unfortunately, Android Studio and the ADT don't fully support tests with Dynamic Features (DF).

https://issuetracker.google.com/issues/123441249
https://issuetracker.google.com/issues/133624929
https://issuetracker.google.com/issues/115569342
https://issuetracker.google.com/issues/132906456
https://issuetracker.google.com/issues/125437348

The following matrix shows the support for each type of test suites:

| From/What                 | Android Studio  | ./gradlew* |  CI (Bitrise/FirebaseLab)  |
|---------------------------|-----------------|------------|----------------------------|
| DF (unit tests)           |       ✓         |     x      |             x              |
| DF (android tests)        |       X         |     ✓      |             x              |
| App Module (unit tests)   |       ✓         |     ✓      |             ✓              |
| App Module (android tests)|       ✓         |     ✓      |             ✓              |

DF modules are: clientapi, fingeprint, face
App module is: id

*To run android tests for a specific DF module: ./gradlew _name_of_df_module_:cAT

During the development of new android tests in DF modules, it can be useful to run a specific test only.
That is possible marking the test with @SmallTest annotation and run ./gradlew _name_of_df_module_:cAT  -Pandroid.testInstrumentationRunnerArguments.size=small

### Limitations

As mentioned above, android tests in DF modules can be launched only from gradlew.
If Mockito lib is used, they can be launched only on devices with API 28 and above.

Normally, Mockito-inline and Mockito-android can mock `val` properties (e.g. whenever(mock) { val_property } thenReturn "some_value") and they achieve that modifying the Dex files (e.g. removing the `final` attribute).
Unfortunately, the this approach doesn't work on DF modules.
To use Mockito in DF modules for android tests, a different dexer is required: com.linkedin.dexmaker:dexmaker-mockito-inline and it requires API 28 (https://github.com/linkedin/dexmaker#mocking-final-classes--methods).

### Technical documentation on features

More about each feature or how each module work can be seen inside every module README files or inside respective folders. Higher level features that touch all modules are documented in the [id module](id/README.md).

## Build types
Simprints ID has 3 different build types: `debug`, `staging` and `release`.
- `debug`: to be used only in development. Uses the development environment of the backend
- `staging`: to be used only for pre-release internal tests. Uses the staging environment of the backend
- `release`: to be used only in production. Uses the production environment of the backend

## Flavours
The app has 2 flavours: `standard` and `withLogFile`.
- `standard`: a normal version of the app, without exporting log files to the device.
- `withLogFile`: all logs are exported to a file in the downloads folder. Requires permission to write to the device's external storage

## Building the app (command line)
If you want to build the app via command line you'll need to provide a combination of a flavour and a build type.

The examples below will show how to create app bundles using `./gradlew`.
To create apks, use the `assemble` command instead of `bundle`.

| Command                   | Debuggable      | Logs to file|  Environment  |
|---------------------------|-----------------|-------------|---------------|
| `bundleStandardDebug`     |       ✓         |     x       |  development  |
| `bundleWithLogFileDebug`  |       ✓         |     ✓       |  development  |
| `bundleStandardStaging`   |       x         |     x       |    staging    |
| `bundleWithLogFileStaging`|       x         |     ✓       |    staging    |
| `bundleStandardRelease`   |       x         |     x       |  production   |
| `bundleWithLogFileRelease`|       x         |     ✓       |  production   |

## Creation of universal apk
To create an universal apk that can be shared you need to:

1. Create a bundle of the app
`./gradlew clean bundleStandardDebug`

2. Create a universal apk that can be installed in any device (warning: this is a big app)
`bundletool build-apks --bundle=id/build/outputs/bundle/standard/debug/id-debug.aab --output=id-standard-debug.apks --ks=debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --mode=universal --overwrite`

To install [bundletool](https://github.com/google/bundletool) you can download the jar from Github and execute it using `java -jar bundletool` or install using Homebrew (on macOS).
