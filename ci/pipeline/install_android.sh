#!/bin/bash

# Thanks to https://gist.github.com/wenzhixin/43cf3ce909c24948c6e7
# Execute this script in your home directory. Lines 17 and 21 will prompt you for a y/n

ANDROID_COMPILE_SDK=29
ANDROID_BUILD_TOOLS=29.0.3
ANDROID_SDK_TOOLS=6858069

apt-get install -y unzip make expect # NDK stuff

# Get SDK tools (link from https://developer.android.com/studio/index.html#downloads)
if [ ! -d "./android" ] 
then
	echo "Android cache doesn't exist"
	mkdir android
	cd android
	wget -nv https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
	unzip -q -d cmdline-tools commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
	mv cmdline-tools/cmdline-tools cmdline-tools/tools
	mkdir sdks

	echo "Android sdks installed!"
else 
	echo "Android cache does exist"
fi

echo "Installing ndk${ANDROID_NDK}"

echo y | sdkmanager "platform-tools" "platforms;android-${ANDROID_COMPILE_SDK}" --sdk_root="~/android/sdks"
echo y | sdkmanager "build-tools;${ANDROID_BUILD_TOOLS}" --sdk_root="~/android/sdks"
echo y | sdkmanager ndk-bundle --sdk_root="~/android/sdks"

yes | sdkmanager --licenses