package com.simprints.id.data.prefs.sessionState

import android.content.Context
import android.os.Build
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManager
import com.simprints.id.domain.Location
import com.simprints.id.data.prefs.preferenceType.ComplexPreference
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.id.tools.serializers.Serializer


class SessionStatePreferencesManagerImpl(private val context: Context,
                                         prefs: ImprovedSharedPreferences,
                                         scannerAttributes: ScannerAttributesPreferencesManager,
                                         sessionParameters: SessionParametersPreferencesManager,
                                         sessionTimestamps: SessionTimestampsPreferencesManager,
                                         locationSerializer: Serializer<Location>)
    : SessionStatePreferencesManager,
    ScannerAttributesPreferencesManager by scannerAttributes,
    SessionParametersPreferencesManager by sessionParameters,
    SessionTimestampsPreferencesManager by sessionTimestamps {

    companion object {
        private const val SESSION_ID_KEY = "SessionId"
        private const val SESSION_ID_DEFAULT = ""

        private const val LOCATION_KEY = "Location"
        private val LOCATION_DEFAULT = Location("", "")
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

    // Last known location
    override var location: Location
        by ComplexPreference(prefs, LOCATION_KEY, LOCATION_DEFAULT, locationSerializer)

    override fun initializeSessionState(sessionId: String, msSinceBootOnSessionStart: Long) {
        this.sessionId = sessionId
        location = LOCATION_DEFAULT
        resetScannerAttributes()
        resetSessionParameters()
        initializeSessionTimestamps(msSinceBootOnSessionStart)
    }
}
