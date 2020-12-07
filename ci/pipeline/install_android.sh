#!/bin/bash

ANDROID_COMPILE_SDK=29
ANDROID_BUILD_TOOLS=29.0.3
ANDROID_SDK_TOOLS=6858069

apt-get install -y unzip make expect # NDK stuff		

SDK_MANAGER="$PWD/android/cmdline-tools/tools/bin/sdkmanager"

echo "sdk.dir=$PWD/android" >> local.properties
echo "ndk.dir=$PWD/android/sdks/ndk-bundle" >> local.properties

mkdir android
cd android
wget -nv https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
unzip -q -d cmdline-tools commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
mv cmdline-tools/cmdline-tools cmdline-tools/tools
mkdir sdks

echo y | $SDK_MANAGER "platform-tools" "platforms;android-${ANDROID_COMPILE_SDK}" --sdk_root="./sdks"
echo y | $SDK_MANAGER "build-tools;${ANDROID_BUILD_TOOLS}" --sdk_root="./sdks"
echo y | $SDK_MANAGER ndk-bundle --sdk_root="./sdks"

yes | $SDK_MANAGER --licenses