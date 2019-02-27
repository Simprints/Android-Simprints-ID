package com.simprints.id.data.prefs.sessionState

import android.content.Context
import android.os.Build
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManager
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName


class SessionStatePreferencesManagerImpl(private val context: Context,
                                         prefs: ImprovedSharedPreferences,
                                         scannerAttributes: ScannerAttributesPreferencesManager,
                                         sessionParameters: SessionParametersPreferencesManager,
                                         sessionTimestamps: SessionTimestampsPreferencesManager)
    : SessionStatePreferencesManager,
    ScannerAttributesPreferencesManager by scannerAttributes,
    SessionParametersPreferencesManager by sessionParameters,
    SessionTimestampsPreferencesManager by sessionTimestamps {

    companion object {
        private const val SESSION_ID_KEY = "SessionId"
        private const val SESSION_ID_DEFAULT = ""

        private const val LOCATION_KEY = "Location"
    }

    override val androidSdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override val deviceId: String
        get() = context.deviceId

    override val appVersionName: String
        get() = context.packageVersionName

    // Unique identifier of the current session
    override var sessionId: String
        by PrimitivePreference(prefs, SESSION_ID_KEY, SESSION_ID_DEFAULT)

    override fun initializeSessionState(sessionId: String, msSinceBootOnSessionStart: Long) {
        this.sessionId = sessionId
        resetScannerAttributes()
        resetSessionParameters()
        initializeSessionTimestamps(msSinceBootOnSessionStart)
    }
}
