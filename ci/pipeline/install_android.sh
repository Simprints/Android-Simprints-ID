#!/bin/bash

# Thanks to https://gist.github.com/wenzhixin/43cf3ce909c24948c6e7
# Execute this script in your home directory. Lines 17 and 21 will prompt you for a y/n

ANDROID_COMPILE_SDK=29
ANDROID_BUILD_TOOLS=29.0.3
ANDROID_SDK_TOOLS=6858069

apt-get install -y unzip make expect # NDK stuff

# Get SDK tools (link from https://developer.android.com/studio/index.html#downloads)
wget -q https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
unzip -q -d android-sdk-linux commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip

export ANDROID_HOME=$PWD/android-sdk-linux
export ANDROID_SDK_ROOT=$PWD/android-sdk-linux
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/bin

source ~/.bashrc

ls $ANDROID_SDK_ROOT
ls $PATH
ls $ANDROID_SDK_ROOT/cmdline-tools/bin

echo y | sdkmanager "platform-tools" "platforms;android-${ANDROID_COMPILE_SDK}" >/dev/null
echo y | sdkmanager "build-tools;${ANDROID_BUILD_TOOLS}" >/dev/null
yes | sdkmanager --licenses

echo $ANDROID_SDK_ROOT
