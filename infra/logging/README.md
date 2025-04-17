# Logging (a.k.a Simber)

The logging module is a very lightweight wrapper in case we ever decide to use a different logging tool

- Clearly document how logging behaves with each build type.
- Isolate the logging tool, Firebase Crashlytics and Firebase Analytics dependencies to a single module. This will make it significantly
  easier for custom versions of SID to remove dependencies on Firebase if they need to.

Access to logging is done solely through the Simber class. See [Simber.kt](src/main/java/com/simprints/infra/logging/Simber.kt) to get
started.

Refs:

* See [Crashlytics](https://firebase.google.com/docs/crashlytics/customize-crash-reports?platform=android)
* See [Firebase Analytics](https://firebase.google.com/docs/analytics/user-properties?platform=android)
