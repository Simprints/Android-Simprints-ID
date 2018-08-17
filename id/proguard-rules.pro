# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/alan/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

#okhttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.simprints.id.secure.models.** { *; }
-keep class com.simprints.id.domain.consent.** { *; }
-keep class com.simprints.id.domain.Project { *; }
-keep class com.simprints.id.session.Session { *; }
-keep class com.simprints.id.data.db.remote.models.** { *; }
-keep class com.simprints.id.data.db.sync.models** { *; }

# For Realm
-keepnames public class * extends io.realm.RealmObject

# These contain serialised models
-keep class com.simprints.libsimprints.** { *; }
-keep class com.simprints.libcommon.** { *; }

# Deobfuscations for Crashlytics:
# https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep public class * extends java.lang.Throwable
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# Dagger
-dontwarn com.google.errorprone.annotations.**

