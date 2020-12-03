#!/bin/bash

ANDROID_COMPILE_SDK=29
ANDROID_BUILD_TOOLS=29.0.3
ANDROID_SDK_TOOLS=6858069

apt-get install -y unzip make expect # NDK stuff

mkdir android
cd android
wget -nv https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
unzip -q -d cmdline-tools commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
mv cmdline-tools/cmdline-tools cmdline-tools/tools
mkdir sdks

echo y | sdkmanager "platform-tools" "platforms;android-${ANDROID_COMPILE_SDK}" --sdk_root="./sdks"
echo y | sdkmanager "build-tools;${ANDROID_BUILD_TOOLS}" --sdk_root="./sdks"
echo y | sdkmanager ndk-bundle --sdk_root="./sdks"

yes | sdkmanager --licenses
