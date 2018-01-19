# Android-Simprints-ID

## Cloning

`git clone --recursive git@github.com:Simprints/Android-Simprints-ID.git`

Temporarily, until the libsimprints git submodule makes it to master:
`git submodule update --init --recursive`

## Using your own development GCP project

By default, any debug build of Simprints ID will interact with the `simprints-dev` GCP project.

To change that behaviour:
- Add the following property into your `gradle.properties` (in `~/.gradle` on Linux systems)
```
development_gcp_project=[YOUR_GCP_PROJECT_ID]
```
- Download the google-services.json file of your GCP project, rename it as 
`[YOUR_GCP_PROJECT_ID]-google-services.json` and place it in id/src/debug/googleServicesJsons (it won't be committed thanks to the .gitignore)

- Rebuild. Gradle might complain at first, but the build should eventually succeed.

Note: When switching from a GCP project to another, it's recommended to do a clean reinstall of the app.
