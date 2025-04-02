# Android-Simprints-ID

## Technical documentation

A high level documentation on how each module works can be found in
the [project's wiki](https://simprints.gitbook.io/docs/architecture/system-architecture/mobile/simprints-id-sid).
More details about each feature or how each module work can be seen inside every module README files or inside respective folders.

## Development setup

### Prerequisites

- **Git**: Ensure you have Git installed to clone the repository.
- **Android Studio**: Install the latest version of Android Studio.
- **GitHub Account**: You will need a GitHub account to generate a token.
- **[KtLint](https://pinterest.github.io/ktlint/latest/install/setup/)**: To ensure consistent code style.

### 1. Clone the Repository

Start by cloning the SID repository to your local machine:

```bash
git clone https://github.com/Simprints/Android-Simprints-ID.git
cd Android-Simprints-ID
```

### 2. Generate a GitHub Token

To access GitHub packages, you need to generate a GitHub token with the `read:packages` scope:

1. Visit [GitHub Tokens](https://github.com/settings/tokens/new) and create a "Classic" token.
2. Ensure the `read:packages` scope is selected.
3. Enable SSO (Single Sign-On) for the token to access the Simprints GitHub organization.
4. Copy the generated token.

### 3. Add the GitHub Token to `local.properties`

Add your GitHub credentials to the `local.properties` file in the project’s root directory:

```properties
GITHUB_USERNAME=<your-github-username>
GITHUB_TOKEN=<the-token-you-just-created>
```

Replace `<your-github-username>` and `<the-token-you-just-created>` with your actual GitHub username and the token you generated.

### 4. Download Required Files

For security reasons, some files are not included in the repository. You must download them separately:

1. Download the necessary files from the
   internal [SID Development Resources folder](https://drive.google.com/drive/folders/1OLrGhx3AW91ab2zduy8FzNuE5r5VEs7g?usp=drive_link).
    - **Note:** This link is accessible to Simprints employees only.
2. Place the downloaded files as follows:
    - `signing_info.gradle.kts`: Place this in the `build-logic` directory.
    - `debug.keystore`: Place this in the root directory of the project.
    - `google-services.json`: Place this in the `id/src` directory.

### 5. Build and Run the App

Open the project in Android Studio, sync the project with Gradle files, and then build and run the app on your connected device or emulator.

## Full CI Workflow

The aim of the `ci` workflow is to run all tests in all modules, assemble production and debug builds of the APK, and report to the main CI
Slack channel.
When run, it immediately triggers all the other relevant workflows such that all tests are run.
In the mean-time, the `ci` build waits for the other workflows to finish and, if they pass, continue to the assemble and deploy steps.

It is triggered upon pull requests and serves as validation of the integrity of the branch for any pull requests into `main`.

<br>

## Testing

### How to run tests

To run all tests in all modules, run `./gradlew testDebugUnitTest` from the root directory.
To run test for a specific module, run `./gradlew moduleName:testDebugUnitTest` for example to run tests for the id module run
`./gradlew id:testDebugUnitTest`

<br>

## Build types

Simprints ID has 3 different build types: `debug`, `staging` and `release`.

- `debug`: to be used only in development. Uses the development environment of the backend
- `staging`: to be used only for pre-release internal tests. Uses the staging environment of the backend, and can be used to test production
  builds both locally and on the Playstore internal channel
- `release`: to be used only in production. Uses the production environment of the backend

## Building the app (command line)

The examples below will show how to create app bundles using `./gradlew`.
To create apks, use the `assemble` command instead of `bundle`.

| Command         | DEBUG_MODE | Encrypted DBs | Debug Features (i.e debug activity) | Backend              |
|-----------------|------------|---------------|-------------------------------------|----------------------|
| `bundleDebug`   | ✓          | x             | ✓                                   | development  						  |
| `bundleStaging` | ✓          | x             | ✓                                   | staging    						    |
| `bundleRelease` | x          | ✓             | x                                   | production (CI only) |

<br>

## Creation of universal apk

To create an universal apk that can be shared you need to:

1. Create a bundle of the app `./gradlew clean bundleDebug`

2. Create a universal apk that can be installed in any device (warning: this is a big app)
   `bundletool build-apks --bundle=id/build/outputs/bundle/debug/id-debug.aab --output=id-debug.apks --ks=debug.keystore --ks-pass=pass:android --ks-key-alias=androiddebugkey --mode=universal --overwrite`

To install [bundletool](https://github.com/google/bundletool) you can download the jar from Github and execute it using
`java -jar bundletool` or install using Homebrew (on macOS).

<br>

## Deploying to a connected device

After creating the universal apk from app bundle, run command to install apk on connected device.
`bundletool install-apks --apks=/MyApp/my_app.apks`

<br>

## Deploying to the Google Play Store and Firebase distribution

Deploying to the Google Play Store and Firebase distribution has several steps. For a full guide
go [here](.github/workflows/README.md) to get more details. 
