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

| From/What                 | Android Studio  | ./gradlew* | CI (Firebase Test Lab) |
|---------------------------|-----------------|------------|------------------------|
| DF (unit tests)           |       ✓         |     x      |          x             |
| DF (android tests)        |       X         |     ✓      |          x             |
| App Module (unit tests)   |       ✓         |     ✓      |          ✓             |
| App Module (android tests)|       ✓         |     ✓      |          ✓             |

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
- `staging`: to be used only for pre-release internal tests. Uses the staging environment of the backend, and can be used to test production builds both locally and on the Playstore internal channel
- `release`: to be used only in production. Uses the production environment of the backend

## Building the app (command line)
The examples below will show how to create app bundles using `./gradlew`.
To create apks, use the `assemble` command instead of `bundle`.

| Command                   |  DEBUG_MODE      | Encrypted DBs |   Logging   | Debug Features (i.e debug activity) |  Backend                              |
|---------------------------|-----------------|----------------|-------------|-------------------------------------|----------------------------------------|
| `bundleDebug`             |       ✓         |      x          |     ✓      |                ✓                    |  development  						    |
| `bundleStaging`           |       ✓         |      x          |     ✓       |               ✓                    |  staging    						     |
| `bundleRelease`           |       x         |      ✓          |     x       |                x                    |  production (CI only) |

## Creation of universal apk
To create an universal apk that can be shared you need to:

1. Create a bundle of the app
`./gradlew clean bundleDebug`

2. Create a universal apk that can be installed in any device (warning: this is a big app)
`bundletool build-apks --bundle=id/build/outputs/bundle/debug/id-debug.aab --output=id-debug.apks --ks=debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --mode=universal --overwrite`

To install [bundletool](https://github.com/google/bundletool) you can download the jar from Github and execute it using `java -jar bundletool` or install using Homebrew (on macOS).

## Deploying to the Google Play Store
For a full guide go [here](https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+a+new+version) to get the complete breakdown. 

Deploying to the Google Play Store has several steps:

1. Update the VersionCode and VersionName 

2. Building and signing a release bundle

3. Uploading to the Google Play Store. 

These steps are done automatically with [Bitbuket Piplines Deployments](https://bitbucket.org/simprints/android-simprints-id/addon/pipelines/deployments). 
Pipelines in a release branch will have an extra step which can automatically upload the release to the internal test track. 
