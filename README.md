# Android-Simprints-ID

## Architecture

The goal of Simprints Architecture is to keep the app maintainable, scalable, easy to work on by multiple people and teams simultaneously, and most importantly - simple. **This architecture is a work in progress, it reflects the planned future state of the repo, it does not reflect its current setup.**

### Modules

There are 2 types of modules we can use, a Feature Module and a Infrastructure Module, with a 3rd type which is the App Module of which there will only ever be 1 of (the ID module). This design has been adopted in several places, and there is even a guide you can check out [here](https://dev.to/noureldinshobier/building-scalable-flutter-apps-architecture-styling-conventions-state-management-40c9) that loosely breaks down a similar architecture. But keep in mind it’s not identical. Following this breakdown a very simple application comprised of a login screen could look like this:

- App (app module)
- feature_login (login UI/UX)
- inf_login (login infrastructure code)
- inf_networking (networking infrastructure code)

### Feature Modules

Feature modules contain the logical boundaries for the user interaction components of a given feature in SID. For example these modules will contain the activities, fragments, views, layouts, and navigation components of a specific feature in SID. They should not contain reusable business logic, or things relating to infrastructure outside of the UX of the feature, those should all be inside of infrastructure modules. You might have two identically themed modules because of this which is fine. For example you could have a Remote Config feature module, which contains the screens and components that interact with the user, and a Remote Config infrastructure module, which contains the infrastructure for fetching, updating and storing the remote configuration. Feature modules will have a couple rules:

Rules:
- Feature modules can depend on the App Module (if they’re a dynamic feature module) and any number of infrastructure modules, but they cannot depend on other feature modules. 
  - *Note: The circular dependency between DFMs and the App module is baked into the system for some reason. This is not a logic/design based dependency. Melad is going to explore this on a future learning day.* 
- Feature modules are accountable for their own internal navigation (they should use a [nested nav graph](https://developer.android.com/guide/navigation/navigation-design-graph#nested-graphs)) and the back stack for their UI. They should have a single point of entry and exit.  
- Feature modules can be a [dynamic feature module](https://developer.android.com/guide/playcore/feature-delivery/on-demand) or a normal Android module, depending on its delivery needs.  

### Infrastructure Modules

Infrastructure Modules contain business logic that should be logically isolated for cleanliness and re-use. Unlike feature modules they should be completely unaware of any UX components such as UIs or navigation. Their goal is to encapsulate some part of the domain layer of the application and make it re-usable to other components. Infrastructure modules will have a couple rules:

Rules: 
- Infrastructure modules should have a single access point which is the contract for the behavior of the module. This should be a top level interface file that exposes the functionality of the module. All other files (except those related to building) should be marked internal and not exposed to its users. 
- Infrastructure modules can’t depend on feature modules or the App module, but they can depend on other infrastructure modules (keep in mind the dependencies can’t be circular) 
- Infrastructure modules preferably are raw Java modules, but they can be Android modules if they need access to certain android components. This is fine for modules that use Jetpack components like Room or Datastore, but they cannot use any components relating to user interactions, such as Activities, Fragments, UI/Layout, or navigation components. 

### The App Module 

In general you don’t have to worry about this section as there can only be one app module (called ID) which is the top level Android Application module. It’s role is to expose the Android manifest, do the initial navigation of the app and house the main Application class. 

Rules:
- The app module should not contain any major business or domain logic, it should just be a thin layer to handle the app navigation and any Android specific requirements such as declaring the apps min API level, etc. 

### SID Breakdown

Following the guidelines above the end goal of SID should look roughly like: 

- feature_about
- feature_clientapi
- feature_dashboard
- feature_eventsystem
- feature_face
- feature_fingerprint
- feature_login
- feature_syncinfo
- id
- infra_eventsystem
- infra_facematcher_roc
- infra_fingerprintmatcher
- infra_fingerprintscanner
- infra_fingerprintscannermock
- infra_logging
- infra_login
- infra_remoteconfig
- infra_network
- infra_security

*Note: There is no longer a core module. There should be no "catch all" module, because it will just become a graveyard / completely overused, like the previous ID module. Every module should have a clear singular purpose.* </br>

<br>

## Cloning

`git clone  git@bitbucket.org:simprints/android-simprints-id.git`  

<br>

## Full CI Workflow

The aim of the `ci` workflow is to run all tests in all modules, assemble production and debug builds of the APK, and report to the main CI Slack channel.
When run, it immediately triggers all the other relevant workflows such that all tests are run.
In the mean-time, the `ci` build waits for the other workflows to finish and, if they pass, continue to the assemble and deploy steps.

It is triggered upon pull requests and serves as validation of the integrity of the branch for any pull requests into `main`. 

<br>

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

* To run android tests for a specific DF module: ./gradlew _name_of_df_module_:cAT
* To run android tests for a specific class in a DF module: ./gradlew _name_of_df_module_:cAT -Pandroid.testInstrumentationRunnerArguments.class=com.simprints.**.ClassName
* During the development of new android tests in DF modules, it can be useful to run specific tests only.
  That is possible marking the test with @SmallTest annotation and run ./gradlew _name_of_df_module_:cAT  -Pandroid.testInstrumentationRunnerArguments.size=small

### Limitations

As mentioned above, android tests in DF modules can be launched only from gradlew.
If Mockito lib is used, they can be launched only on devices with API 28 and above.

Normally, Mockito-inline and Mockito-android can mock `val` properties (e.g. whenever(mock) { val_property } thenReturn "some_value") and they achieve that modifying the Dex files (e.g. removing the `final` attribute).
Unfortunately, the this approach doesn't work on DF modules.
To use Mockito in DF modules for android tests, a different dexer is required: com.linkedin.dexmaker:dexmaker-mockito-inline and it requires API 28 (https://github.com/linkedin/dexmaker#mocking-final-classes--methods).

### Technical documentation on features

A high level documentation on how each module works can be found in the [project's wiki](https://bitbucket.org/simprints/android-simprints-id/wiki/Home) on bitbucket.
More details about each feature or how each module work can be seen inside every module README files or inside respective folders. Higher level features that touch all modules are documented in the [id module](id/README.md). 

<br>

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

<br>

## Creation of universal apk

To create an universal apk that can be shared you need to:

1. Create a bundle of the app
`./gradlew clean bundleDebug`

2. Create a universal apk that can be installed in any device (warning: this is a big app)
`bundletool build-apks --bundle=id/build/outputs/bundle/debug/id-debug.aab --output=id-debug.apks --ks=debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --mode=universal --overwrite`

To install [bundletool](https://github.com/google/bundletool) you can download the jar from Github and execute it using `java -jar bundletool` or install using Homebrew (on macOS). 

<br>

## Deploying to a connected device

After creating the universal apk from app bundle, run command to install apk on connected device.
`bundletool install-apks --apks=/MyApp/my_app.apks` </br>

## Deploying to the Google Play Store

For a full guide go [here](https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+a+new+version) to get the complete breakdown. 

Deploying to the Google Play Store has several steps:

1. Update the VersionCode and VersionName 

2. Building and signing a release bundle

3. Uploading to the Google Play Store. 

These steps are done automatically with [Bitbuket Piplines Deployments](https://bitbucket.org/simprints/android-simprints-id/addon/pipelines/deployments). 
Pipelines in a release branch will have an extra step which can automatically upload the release to the internal test track. 
