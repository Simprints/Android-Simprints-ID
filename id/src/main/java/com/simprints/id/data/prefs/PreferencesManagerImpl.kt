package com.simprints.id.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_FILE_NAME
import com.simprints.core.tools.constants.SharedPrefsConstants.PREF_MODE
import com.simprints.core.sharedpreferences.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.SecureLocalDbKeyProviderImpl.Companion.SHARED_PREFS_KEY_FOR_REALM_KEY_IDENTIFIER


class PreferencesManagerImpl(settings: SettingsPreferencesManager,
                             lastEvents: RecentEventsPreferencesManager,
                             context: Context)
    : PreferencesManager,
    SettingsPreferencesManager by settings,
    RecentEventsPreferencesManager by lastEvents {

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

    override fun clearAllSharedPreferencesExceptRealmKeys() {
        prefs.all.forEach {
            if (!containsRealmKey(it)) {
                prefs.edit().remove(it.key).apply()
            }
        }
    }

    private fun containsRealmKey(it: Map.Entry<String, Any?>) =
        it.key.contains(SHARED_PREFS_KEY_FOR_REALM_KEY_IDENTIFIER)
}
