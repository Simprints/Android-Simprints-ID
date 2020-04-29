# Android-Simprints-ID

## Cloning

There are submodules in this repository [LibSimprints](https://github.com/Simprints/LibSimprints) and [SimMatcher](https://github.com/Simprints/Fingerprint-SimMatcher), so recursive cloning is necessary:

`git clone --recursive git@github.com:Simprints/Android-Simprints-ID.git`


## Bitrise CI
The continuous integration system is hosted on [Bitrise](https://app.bitrise.io/app/593e8eaa8d43a51b#/builds), with a number of available build containers with which to run builds in parallel.
The configuration for build workflows is split between between a **remote** configuration, which lives on the Bitrise website, and a **local** configuration in the form of `bitrise.yml` which is found here in the root of the repository.

Extra details on this method of managing the CI system can be found [here](https://devcenter.bitrise.io/tips-and-tricks/use-bitrise-yml-from-repository/#setting-up-a-wrapper-configuration).

### Remote Bitrise Config
The [remote config](https://app.bitrise.io/app/593e8eaa8d43a51b/workflow_editor#!/workflows) is global and un-versioned, so care must be taken when making updates to existing workflows.
All workflows in the remote configuration simply consist of the `run_from_repo` workflow.
The `run_from_repo` workflow when triggered by workflow with name "X" downloads the git repo, looks inside `bitrise.yml` for workflow "X", and executes it.
This means when creating a new workflow in the local repo, one must match it with a workflow in the remote repo with the same name, that simply calls `run_from_repo`.

The remote workflows can be triggered with pull requests and commits. Feel free to specify your own triggers as you like if you're working off of a long-lived branch that isn't `develop`.

### Local Bitrise Config
The local config `bitrise.yml` contains the actual details for running a workflow, such as making the gradle build, running the tests, reporting results and deploying artifacts.
The workflow will run in whichever container was defined by the corresponding remote workflow.

To edit the `bitrise.yml`, follow the steps to download the [Bitrise CLI](https://app.bitrise.io/cli) tool, then in the root of the repository run:
```
bitrise :workflow-editor
```
Which will open the workflow editor running locally in a browser window.
Edits made in the workflow editor will change the `bitrise.yml` correspondingly when saved.
Changes should be committed to git such that the branch always has succeeding relevant workflows.

In general, each module should have its own workflow titled with `modulename_test`, which should run all tests (Unit & Android) relevant to that module, either directly or indirectly.
It is up to the managers of each module to determine whether they would prefer this workflow to run all the tests itself directly, or indirectly by triggering further parallel workflows e.g. `modulename_unit_test` while `modulename_test` runs the Android tests and awaits the unit tests.

Be sure to ensure that any newly added workflows have corresponding remote workflows on Bitrise (see above).

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
