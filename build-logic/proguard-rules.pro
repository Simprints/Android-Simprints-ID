# Dont warn about the missing files during the obfuscation
-dontwarn com.simprints.**
# Do not obfuscate names in simprints package
-keep class com.simprints.** { *; }
