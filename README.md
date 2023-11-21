# Android-Simprints-ID

## Architecture

The goal of Simprints Architecture is to keep the app maintainable, scalable, easy to work on by multiple people and teams simultaneously, and most importantly - simple. **This architecture is a work in progress, it reflects the planned future state of the repo, it does not reflect its current setup.**

### Modules

There are 2 types of modules we can use, a Feature Module and a Infrastructure Module, with a 3rd type which is the App Module of which there will only ever be 1 of (the ID module). This design has been adopted in several places, and there is even a guide you can check out [here](https://dev.to/noureldinshobier/building-scalable-flutter-apps-architecture-styling-conventions-state-management-40c9) that loosely breaks down a similar architecture. But keep in mind it’s not identical. Following this breakdown a very simple application comprised of a login screen could look like this:

- App (app module)
- feature login (login UI/UX)
- infra login (login infrastructure code)
- infra networking (networking infrastructure code)

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
- Infrastructure modules will depend on the builder pattern. There will be two top level nome inner classes, the interface the exposes the functionality, and the builder object that is able to build the implementation of that interface. Say for the *infralogin* module we have a *LoginManager* interface we'd also have a top level *LoginManagerBuilder* class which is able to build the required implementation of that interface. It doesn't matter what that builder itself uses for DI (such as dagger ), and same for the other builders that call it.
- Infrastructure modules can’t depend on feature modules or the App module, but they can depend on other infrastructure modules (keep in mind the dependencies can’t be circular).
- Infrastructure modules preferably are raw Java modules, but they can be Android modules if they need access to certain android components. This is fine for modules that use Jetpack components like Room or Datastore, but they cannot use any components relating to user interactions, such as Activities, Fragments, UI/Layout, or navigation components.

### The App Module

In general you don’t have to worry about this section as there can only be one app module (called ID) which is the top level Android Application module. It’s role is to expose the Android manifest, do the initial navigation of the app and house the main Application class.

Rules:
- The app module should not contain any major business or domain logic, it should just be a thin layer to handle the app navigation and any Android specific requirements such as declaring the apps min API level, etc.

### SID Breakdown

Following the guidelines above the end goal of SID should look roughly like:

- featureabout (com.simprints.feature.about)
- featureclientapi (com.simprints.feature.clientapi)
- featuredashboard (com.simprints.feature.dashboard)
- featureface (com.simprints.feature.face)
- featurefingerprint (com.simprints.feature.fingerprint)
- featurelogin (com.simprints.feature.login)
- featuresyncinfo (com.simprints.feature.syncinfo)
- id
- infraconfig (com.simprints.infra.config)
- infraenrolmentrecords (com.simprints.infra.enrolment.records)
- infraevents (com.simprints.infra.events)
- infrafacematcher_roc (com.simprints.infra.facematcher)
- fingerprint/infra/matcher (com.simprints.fingerprint.infra.matcher)
- fingerprint/infra/scanner (com.simprints.fingerprint.infra.scanner)
- fingerprint/infra/scannermock (com.simprints.fingerprint.infra.scannermock)
- infraimages (com.simprints.infra.images)
- infralicense (com.simprints.infra.license)
- infralogging (com.simprints.infra.logging)
- infralogin (com.simprints.infra.login)
- infranetwork (com.simprints.infra.networking)
- infrarealm (com.simprints.infra.realm)
- infrasecurity (com.simprints.infra.security)

*Note: There is no longer a core module. There should be no "catch all" module, because it will just become a graveyard / completely overused, like the previous ID module. Every module should have a clear singular purpose.* </br>

<br>

## Development setup

Clone with `git clone https://github.com/Simprints/Android-Simprints-ID.git`

Ensure the Gradle Offline Mode is disabled:

![Disable Gradle Offline Mode](./img/disable-gradle-offline-mode.jpg)

Generate a new Github token at <https://github.com/settings/tokens/new>, which generates a "Classic" token. The only scope needed is `read:packages`. Then add it to the `local.properties` file:

```properties
GITHUB_USERNAME=<your-github-username>
GITHUB_TOKEN=<the-token-you-just-created>
```

## Full CI Workflow

The aim of the `ci` workflow is to run all tests in all modules, assemble production and debug builds of the APK, and report to the main CI Slack channel.
When run, it immediately triggers all the other relevant workflows such that all tests are run.
In the mean-time, the `ci` build waits for the other workflows to finish and, if they pass, continue to the assemble and deploy steps.

It is triggered upon pull requests and serves as validation of the integrity of the branch for any pull requests into `main`.

<br>

## Testing

### How to run tests

To run all tests in all modules, run `./gradlew testDebugUnitTest` from the root directory.
To run test for a specific module, run `./gradlew moduleName:testDebugUnitTest` for example to run tests for the id module run `./gradlew id:testDebugUnitTest`

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
`bundletool install-apks --apks=/MyApp/my_app.apks`

<br>

## Deploying to the Google Play Store and Firebase distribution

Deploying to the Google Play Store and Firebase distribution has several steps. For a full guide
go [here](.github/workflows/README.md) to get more details. 
