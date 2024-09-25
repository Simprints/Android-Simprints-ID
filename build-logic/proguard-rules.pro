# Dont warn about the missing files during the obfuscation
-dontwarn com.simprints.**
# Do not obfuscate the simprints package
-keep class com.simprints.** { *; }
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
