package com.simprints.id.data.prefs.sessionState

import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManager


interface SessionStatePreferencesManager
    : SessionParametersPreferencesManager,
    ScannerAttributesPreferencesManager,
    SessionTimestampsPreferencesManager {

    val androidSdkVersion: Int
    val deviceModel: String
    var sessionId: String
    val deviceId: String
    val appVersionName: String
    fun initializeSessionState(sessionId: String, msSinceBootOnSessionStart: Long)
}
