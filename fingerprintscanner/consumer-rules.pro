# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/ethiery/Android/Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# We need to keep the vero and UN20 models because the scanner sdk uses reflection to create these
# classes from the message stream.
-keep class com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.* { *; }
-keep class com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.* { *; }
