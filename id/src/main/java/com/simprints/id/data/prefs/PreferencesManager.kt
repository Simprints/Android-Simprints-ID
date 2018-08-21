package com.simprints.id.data.prefs

import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.sync.people.SyncPeoplePreferencesManager
import com.simprints.id.data.prefs.sync.sessions.SyncSessionsPreferencesManager

interface PreferencesManager :
    SessionStatePreferencesManager,
    SettingsPreferencesManager,
    RecentEventsPreferencesManager,
    SyncPeoplePreferencesManager,
    SyncSessionsPreferencesManager {

    fun <T> getSharedPreference(key: String, defaultValue: T): T

    fun setSharedPreference(key: String, value: Any)

}
