package com.simprints.id.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager


class PreferencesManagerImpl(sessionState: SessionStatePreferencesManager,
                             settings: SettingsPreferencesManager,
                             lastEvents: RecentEventsPreferencesManager,
                             context: Context)
    : PreferencesManager,
    SessionStatePreferencesManager by sessionState,
    SettingsPreferencesManager by settings,
    RecentEventsPreferencesManager by lastEvents {

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
            is Boolean -> prefs.edit { putBoolean(key, value) }
            is Float -> prefs.edit { putFloat(key, value) }
            is Int -> prefs.edit { putInt(key, value) }
            is Long -> prefs.edit { putLong(key, value) }
            is String -> prefs.edit { putString(key, value) }
        }
    }

}
