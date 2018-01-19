# Android-Simprints-ID

## Cloning

`git clone --recursive git@github.com:Simprints/Android-Simprints-ID.git`

Temporarily, until the libsimprints git submodule makes it to master:
`git submodule update --init --recursive`

## Using your own development GCP project

By default, any debug build of Simprints ID will interact with the `simprints-dev` GCP project.

To change that behaviour:
- download the google-services.json file of the GCP project you want to use
- add the two following properties into your `gradle.properties` (in `~/.gradle` on Linux systems)
```
development_gcp_project=[YOUR_GCP_PROJECT_ID]
dev_google_services_json_path=[ABSOLUTE_PATH_TO_YOUR_GOOGLE_SERVICES_JSON]
```
