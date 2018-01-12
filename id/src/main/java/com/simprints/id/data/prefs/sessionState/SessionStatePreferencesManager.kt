package com.simprints.id.data.prefs.sessionState

import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManager
import com.simprints.id.domain.Location


interface SessionStatePreferencesManager
    : SessionParametersPreferencesManager,
    ScannerAttributesPreferencesManager,
    SessionTimestampsPreferencesManager {

    var sessionId: String
    var location: Location

    fun initializeSessionState(sessionId: String, msSinceBootOnSessionStart: Long)

}
