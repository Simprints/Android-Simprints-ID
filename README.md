# Android-Simprints-ID

## Cloning

`git clone --recursive git@github.com:Simprints/Android-Simprints-ID.git`

Temporarily, until the libsimprints git submodule makes it to master:
`git submodule update --init --recursive`

Add in ~/.gradle/gradle.properties:

`SIMPRINTSID_ARTIFACTORY_USERNAME=`

`SIMPRINTSID_ARTIFACTORY_PASSWORD=`

`SIMPRINTSID_DEV_GCP_PROJECT=simprints-dev`

`SIMPRINTSID_RELEASE_STORE_FILE=`

`SIMPRINTSID_RELEASE_STORE_PASSWORD=`

`SIMPRINTSID_RELEASE_KEY_ALIAS=`

`SIMPRINTSID_RELEASE_KEY_PASSWORD=`

## Testing

Tests can be run in Android Studio, but it requires Cerberus installed and running (foreground) on the devices where tests will be launched.
Alternatively, `run ./instrumented_tests` checkouts and builds Cerberus before launching the instrumented tests on the devices.

#### Requirements for testing:

#####  Gradle properties #####
variables in ~/.gradle/gradle.properties:

`sdk.dir=/..../Android/sdk`

`SIMPRINTSID_TEST_SCANNER=`

`SIMPRINTSID_TEST_WIFI=`

`SIMPRINTSID_TEST_WIFI_PASSWORD=`

#####  ENV #####
ANDROID_HOME set to the Android SDK path
ANDROID_HOME/tools-platform included in PATH

```
echo "export ANDROID_HOME=**YOUR_SDK_PATH**" >> ~/.bash_profile
echo "export PATH=$PATH:$ANDROID_HOME/platform-tools" >> ~/.bash_profile

```


## Using your own development GCP project

By default, any debug build of Simprints ID will interact with the `simprints-dev` GCP project.

To change that behaviour:
- Your development GCP project should be properly setup. To do so follow [the instructions in the Panda wiki](https://sites.google.com/simprints.com/panda-wiki/cloud/set-up-a-development-gcp-project).
- Add the following property into your Global `gradle.properties` (located in `USER_HOME/.gradle`)
```
development_gcp_project=[YOUR_GCP_PROJECT_ID]
```
- Follow [these instructions](https://firebase.google.com/docs/android/setup#manually_add_firebase) to add the App to your GCP / Firebase project and download the corresponding `google-services.json` file (or files, in the case of adding your own Firestore project as well).
 
- Rename this file as `[YOUR_GCP_PROJECT_ID]_google_services.json`, replacing all hyphens with underscores, and place it the folder `id/src/debug/res/raw` (it won't be committed thanks to the .gitignore). If necessary, do the same with the firestore version with the format `[YOUR_GCP_PROJECT_ID]_fs_google_services.json`. 

- Run the file `update_google_services_jsons.sh` located in the root folder. (This copies the appropriate file for each build-type in res/raw to the top of the build-type folder, and renames it `google-services.json`)

- Rebuild. Gradle might complain at first, but the build should eventually succeed.

Note: When switching from a GCP project to another, it's necessary to re-run `update_google_services_jsons.sh` and it's recommended to do a clean reinstall of the app.
