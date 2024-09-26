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

# Kotlin Coroutines
# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembernames class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# Jackson
# Proguard configuration for Jackson 2.x (fasterxml package instead of codehaus package)
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.ObjectWriter {
    public ** writeValueAsString(**);
}
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

#Keep all TypeReferences to preserve generic types
-keep class * extends com.fasterxml.jackson.core.type.TypeReference {
    public <init>(java.lang.reflect.Type);
}

#net.zetetic:android-database-sqlcipher
-keep class net.sqlcipher.** { *; }

# Do not obfuscate names in simprints package
-keep class com.simprints.** { *; }
# Keep all marshallable classes as-is
-keep class com.simprints.** extends java.io.Serializable { *; }
-keep class com.simprints.** extends android.os.Parcelable { *; }

-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }

# Prevent ProGuard from removing @Module, @Inject, @Binds, etc.
-keep class **Module { *; }
-keep class **Inject { *; }
-keep class **Provides { *; }
-keep class **Binds { *; }
-keep class **Hilt_** { *; }

# Keep generated Hilt components
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }

# Keep classes annotated with @InstallIn to prevent them from being removed
-keep @dagger.hilt.InstallIn class * { *; }

# Keep Hilt components and related Dagger generated code
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
