# Preferences

The module presents access to shared preferences and remote config via
[`PreferencesManager`](./PreferencesManager.kt).

## Preference Types

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
(and its [complex counterpart](./preferenceType/remoteConfig/RemoteConfigComplexPreference.kt))
which instead retrieves the setting remotely only. Note that despite
appearing as a `var` in the interface, the setters of these properties
do not change the property as the getter will always retrieve the
property from the remote config. This preference tries first to get the
preference from RemoteConfig. Failing to do that, it returns the default
value that was added to it during creation. This means if the remote
config defaults are not accessible or non existent, then the local
defaults can be used as fall-back. This can occur if settings have been
removed online but older versions of the app are still relying on them.
This is tightly coupled to RemoteConfigWrapper, explained below.

Finally, there is a combination of these preference types:
[`OverridableRemoteConfigPrimitivePreference`](./preferenceType/remoteConfig/overridable/OverridableRemoteConfigPrimitivePreference.kt)
and its [complex counterpart](./preferenceType/remoteConfig/overridable/OverridableRemoteConfigComplexPreference.kt)).
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

## Remote Config

### Remote Config Wrapper

SID downloads settings from BFSID during login and save them as JSON in
shared preferences. Every time a config is needed that has a RemoteConfig
type, SID parses the JSON and return the correct value. If no value is
found the wrapper returns `null` and expects classes using it to know how
to handle that.

### Remote Config Worker

There is a background worker that keeps running and download new
configurations as they are created in BFSID. This worker runs at the same
frequency as other workers by using the value in
`BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES`.

Every time the worker runs the new JSON will be saved in the shared
preference. Next time the config is used it will be the updated version.
