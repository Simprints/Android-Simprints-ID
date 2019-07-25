package com.simprints.id.data.prefs

import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager

interface PreferencesManager :
    SettingsPreferencesManager,
    RecentEventsPreferencesManager {

    fun <T> getSharedPreference(key: String, defaultValue: T): T

    fun setSharedPreference(key: String, value: Any)

    fun clearAllSharedPreferencesExceptRealmKeys()
}
