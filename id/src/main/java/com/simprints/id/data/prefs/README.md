# Preferences

The module presents access to shared preferences and remote config via
[`PreferencesManager`](./PreferencesManager.kt).

### Preference Types

[`SettingsPreferencesManager`](./settings/SettingsPreferencesManager.kt)
is the primary sub-interface that handles persisted settings, that
relies on many [preference types](./preferenceType) to achieve different
persistent behaviours. The base of these types are:

- [`PrimitivePreference`](./preferenceType/PrimitivePreference.kt) -
  This is the most basic preference for saving primitives values that
  performs a straightforward read/write into shared prefs.
- [`ComplexPreference`](./preferenceType/ComplexPreference.kt) - This is
  for saving non-primitive values. It must be passed a `Serializer<T>`
  which converts `T` to and from a `String`. It uses a backing
  `PrimitivePreference` to save the string.

`PrimitivePreference` and `ComplexPreference` act as simple
read/writable preferences that are stored locally only.

There is also
[`RemoteConfigPrimitivePreference`](./preferenceType/remoteConfig/RemoteConfigPrimitivePreference.kt)
(and its
[complex counterpart](./preferenceType/remoteConfig/RemoteConfigComplexPreference.kt))
which instead retrieves the setting remotely only. Note that despite
appearing as a `var` in the interface, the setters of these properties
do not change the property as the getter will always retrieve the
property from the remote config.

Finally, there is a combination of these preference types:
[`OverridableRemoteConfigPrimitivePreference`](./preferenceType/remoteConfig/overridable/OverridableRemoteConfigPrimitivePreference.kt)
and its
[complex counterpart](./preferenceType/remoteConfig/overridable/OverridableRemoteConfigComplexPreference.kt)).
These act like `RemoteConfigPrimitivePreference`s, where the setting is
retrieved from remote config. This is until when the setter on the
property is called at any time, in which case the new value is saved and
from that point onwards it behaves like a `PrimitivePreference` where
the setting is set and retrieved locally. This is useful in situations
where configuration can be mandated via remote config, but the user may
choose to override these options with their own setting (such as the app
language).

The overridable preference works by creating a backing primitive/complex
preference, say with key `"pref_key"`, and an additional meta-preference
with key `"pref_key_isOverridden"` which is a boolean flag that is false
by default. The moment the setter for the preference is called, this
flag is set to true, which controls the subsequent behaviour transition
from a remote-seeking pref to a local one.

### Remote Config

#### Remote Config Wrapper

For preference types that retrieve settings from remote config, the
`FirebaseRemoteConfig` from the Firebase SDK is currently used. This
used is used not only to deliver settings remotely, but also to specify
particular settings on a project-by-project based.

This is achieved by setting a custom User Property in the Firebase
Analytics SDK called `project_id` to the project ID used at Simprints
login. Customisation is then possible thanks to using this user property
as a custom condition for a given project.

The is a problem with this approach - upon login, it may take up to an
hour before the Firebase Analytics custom user properties kick-in. This
is too late for a variety of use cases, as many project-specific actions
need to occur as soon as login succeeds, such as downloading appropriate
dynamic features for the project and syncing appropriate records.

This problem has been solved with the following actions:

1. Upon login, a request is made to our custom back-end to query the
   remote config settings for a project and download and save them all
   as JSON so they can be used immediately by the app.
2. A remote config setting called `ProjectSpecificMode` is set to `true`
   for the project, whilst the default in remote config is
   `false`.

Before the Firebase Analytics user property kicks-in, retrieving the
`ProjectSpecificMode` value from the remote config SDK will yield
`false`. If this is the case, settings are retrieved from the JSON saved
at login. Once `ProjectSpecificMode` becomes `true`, we know that at
this point the analytics user property has become active and we can now
read settings from the remote config SDK, knowing for certain that we
are receiving the custom settings for the project. At this point, online
updates to remote config will eventually be reflected in the remote
config SDK.

This switching is handled by the
[`RemoteConfigWrapper`](./RemoteConfigWrapper.kt). When a setting is
needed from remote config, calls go through this wrapper that checks
`ProjectSpecificMode`. If it is `false` it uses the JSON values; if it
is `true` it uses the remote config SDK values.

The wrapper additionally handles the registering of local defaults. This
means if the remote config defaults are not accessible, then the local
defaults can be used as fall-back. This can occur if settings have been
removed online but older versions of the app are still relying on them.

#### Remote Config Fetcher

The [`RemoteConfigFetcher`](./RemoteConfigFetcher.kt) is used to
schedule a refresh of the remote config values in the remote config SDK.
There is a cache at play meaning that remote refreshes will not occur
more often than 12 hours by default.

To force a refresh e.g. for testing purposes, it's sufficient to log out
and back in.
