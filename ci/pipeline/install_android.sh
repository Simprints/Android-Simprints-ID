#!/bin/bash

# Thanks to https://gist.github.com/wenzhixin/43cf3ce909c24948c6e7
# Execute this script in your home directory. Lines 17 and 21 will prompt you for a y/n

ANDROID_COMPILE_SDK=29
ANDROID_BUILD_TOOLS=29.0.3
ANDROID_SDK_TOOLS=6858069

apt-get install -y unzip make expect # NDK stuff

# Get SDK tools (link from https://developer.android.com/studio/index.html#downloads)
wget -nv https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
unzip -q -d cmdline-tools commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
mv cmdline-tools/cmdline-tools cmdline-tools/tools

export ANDROID_HOME=$PWD/cmdline-tools
export ANDROID_SDK_ROOT=$PWD/cmdline-tools
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/tools/bin

source ~/.bashrc

echo "PATH:"
echo $PATH

echo "ANDROID_SDK_ROOT:"
echo $ANDROID_SDK_ROOT

ls $ANDROID_SDK_ROOT

echo y | sdkmanager --sdk_root=$ANDROID_SDK_ROOT "platform-tools" "platforms;android-${ANDROID_COMPILE_SDK}" >/dev/null
echo y | sdkmanager -sdk_root=$ANDROID_SDK_ROOT "build-tools;${ANDROID_BUILD_TOOLS}" >/dev/null
yes | sdkmanager --licenses

echo $ANDROID_SDK_ROOT
