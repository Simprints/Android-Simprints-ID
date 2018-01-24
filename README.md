# Android-Simprints-ID

## Cloning

`git clone --recursive git@github.com:Simprints/Android-Simprints-ID.git`

Temporarily, until the libsimprints git submodule makes it to master:
`git submodule update --init --recursive`

## Testing
run ./instrumented_tests

#### Requirements:

#####  Gradle properties #####
variables in {ROOT}/local.properties:

`sdk.dir=/..../Android/sdk`

`ext.scanner="SPXXXX"`

`ext.wifiNetwork="XXXXX"`

`ext.wifiPassword="XXXXXX"`

#####  ENV #####
ANDROID_HOME set to the Android SDK path
ANDROID_HOME/tools-platform included in PATH

```
echo "export ANDROID_HOME=**YOUR_SDK_PATH**" >> ~/.bash_profile
echo "export PATH=$PATH:$ANDROID_HOME/platform-tools" >> ~/.bash_profile

```




