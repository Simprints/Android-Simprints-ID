package com.simprints.id.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.sync.people.SyncPeoplePreferencesManager
import com.simprints.id.data.prefs.sync.sessions.SyncSessionsPreferencesManager

class PreferencesManagerImpl(sessionState: SessionStatePreferencesManager,
                             settings: SettingsPreferencesManager,
                             lastEvents: RecentEventsPreferencesManager,
                             syncPeoplePreferencesManager: SyncPeoplePreferencesManager,
                             syncSessionsPreferencesManager: SyncSessionsPreferencesManager,
                             context: Context)
    : PreferencesManager,
    SessionStatePreferencesManager by sessionState,
    SettingsPreferencesManager by settings,
    RecentEventsPreferencesManager by lastEvents,
    SyncPeoplePreferencesManager by syncPeoplePreferencesManager,
    SyncSessionsPreferencesManager by syncSessionsPreferencesManager {

    companion object {
        const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        const val PREF_MODE = Context.MODE_PRIVATE
    }

    val prefs: SharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    @Suppress("UNCHECKED_CAST")
    override fun <T> getSharedPreference(key: String, defaultValue: T): T {
        return prefs.all[key] as T? ?: defaultValue
    }

    override fun setSharedPreference(key: String, value: Any) {
        when (value) {
            is Boolean -> prefs.edit().putBoolean(key, value).apply()
            is Float -> prefs.edit().putFloat(key, value).apply()
            is Int -> prefs.edit().putInt(key, value).apply()
            is Long -> prefs.edit().putLong(key, value).apply()
            is String -> prefs.edit().putString(key, value).apply()
        }
    }
}
