package com.simprints.id.data.prefs

import android.content.Context
import com.simprints.id.data.prefs.events.EventsPreferencesManager
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager

class PreferencesManagerImpl(sessionState: SessionStatePreferencesManager,
                             settings: SettingsPreferencesManager,
                             lastEvents: EventsPreferencesManager)
    : PreferencesManager,
    SessionStatePreferencesManager by sessionState,
    SettingsPreferencesManager by settings,
    EventsPreferencesManager by lastEvents {

    companion object {
        val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        val PREF_MODE = Context.MODE_PRIVATE
    }
}
