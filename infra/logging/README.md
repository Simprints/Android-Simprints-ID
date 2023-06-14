# Logging (a.k.a Simber)

The logging module is a very lightweight wrapper around Timber which has a few goals:
- A very very lightweight wrapper around Jake Wharton’s Timber in case we ever decide to use a different logging tool
- Clearly document how logging behaves with each build type.
- Isolate the Timber, Firebase Crashlytics and Firebase Analytics dependencies to a single module. This will make it significantly easier for custom versions of SID to remove dependencies on Firebase if they need to. 

Access to logging is done solely through the Simber class. See [Simber.kt](src/main/java/com/simprints/logging/Simber.kt) to get started. 

Refs:
See <a href="URL#https://github.com/JakeWharton/timber">Timber</a>
See <a href="URL#https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android">Crashlytics</a>  
See <a href="URL#https://firebase.google.com/docs/analytics/user-properties?platform=android">Firebase Analytics</a>
