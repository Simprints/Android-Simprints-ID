package com.simprints.id.data.prefs

import com.simprints.id.data.prefs.events.EventsPreferencesManager
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager

/**
 * Why an interface if there is a single implementation?
 * Because using the interface makes it super easy to swap the true implementation (which uses the
 * Android Framework and is not a available in a non instrumented test) with a mock
 *
 * @author: Etienne Thiery (etienne@simprints.com)
 */
interface PreferencesManager : SessionStatePreferencesManager, SettingsPreferencesManager, EventsPreferencesManager
