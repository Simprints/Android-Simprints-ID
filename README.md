# Android-Simprints-ID

## Cloning

`git clone --recursive git@github.com:Simprints/Android-Simprints-ID.git`

Temporarily, until the libsimprints git submodule makes it to master:
`git submodule update --init --recursive`

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

###Limitations

As mentioned above, android tests in DF modules can be launched only from gradlew.
If Mockito lib is used, they can be launched only on devices with API 28 and above.

Normally, Mockito-inline and Mockito-android can mock `val` properties (e.g. whenever(mock) { val_property } thenReturn "some_value") and they achieve that modifying the Dex files (e.g. removing the `final` attribute).
Unfortunately, the this approach doesn't work on DF modules.
To use Mockito in DF modules for android tests, a different dexer is required: com.linkedin.dexmaker:dexmaker-mockito-inline and it requires API 28 (https://github.com/linkedin/dexmaker#mocking-final-classes--methods).
