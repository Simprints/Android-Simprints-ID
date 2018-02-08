package com.simprints.id.data

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.models.Session
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.sessionParameters.SessionParameters
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.libcommon.Person
import com.simprints.libdata.*
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification


class DataManagerImpl(private val context: Context,
                      private val preferencesManager: PreferencesManager,
                      private val dbManager: DbManager,
                      private val apiManager: ApiManager,
                      private val analyticsManager: AnalyticsManager,
                      private val secureDataManager: SecureDataManager)
    : DataManager,
    PreferencesManager by preferencesManager,
    AnalyticsManager by analyticsManager,
    DbManager by dbManager,
    ApiManager by apiManager,
    SecureDataManager by secureDataManager {

    override val androidSdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override val deviceId: String
        get() = context.deviceId

    override val appVersionName: String
        get() = context.packageVersionName

    override val libVersionName: String
        get() = com.simprints.libsimprints.BuildConfig.VERSION_NAME

    override var sessionParameters: SessionParameters
        get() = preferencesManager.sessionParameters
        set(value) {
            preferencesManager.sessionParameters = value
            secureDataManager.apiKey = value.apiKey
        }

    override fun logAlert(alertType: ALERT_TYPE) =
        analyticsManager.logAlert(alertType.name, getApiKeyOrEmpty(), moduleId, userId, deviceId)

    override fun logUserProperties() =
        analyticsManager.logUserProperties(userId, getApiKeyOrEmpty(), moduleId, deviceId)

    override fun logScannerProperties() =
        analyticsManager.logScannerProperties(macAddress, scannerId)

    override fun logGuidSelectionService(apiKey: String, sessionId: String, selectedGuid: String,
                                         callbackSent: Boolean) =
        analyticsManager.logGuidSelectionService(apiKey, sessionId, selectedGuid, callbackSent,
            deviceId)

    override fun logConnectionStateChange(connected: Boolean) =
        analyticsManager.logConnectionStateChange(connected, getApiKeyOrEmpty(), deviceId, sessionId)

    override fun logAuthStateChange(authenticated: Boolean) =
        analyticsManager.logAuthStateChange(authenticated, getApiKeyOrEmpty(), deviceId, sessionId)

//    private val connectionStateLogger = object : ConnectionListener {
//        override fun onConnection() = logConnectionStateChange(true)
//        override fun onDisconnection() = logConnectionStateChange(false)
//    }
//
//    private val authStateLogger = object : AuthListener {
//        override fun onSignIn() = logAuthStateChange(true)
//        override fun onSignOut() = logAuthStateChange(false)
//    }

    // DbManager argument interception
    // Remote Only
    override fun updateIdentification(apiKey: String, selectedGuid: String) {
        dbManager.updateIdentificationInRemote(apiKey, selectedGuid, deviceId, sessionId)
    }

    override fun saveSession() {
        val session = Session(sessionId, androidSdkVersion, deviceModel, deviceId, appVersionName,
            libVersionName, calloutAction.toString(), getApiKeyOrEmpty(), moduleId, userId,
            patientId, callingPackage, metadata, resultFormat, macAddress, scannerId,
            hardwareVersion.toInt(), location.latitude, location.longitude,
            msSinceBootOnSessionStart, msSinceBootOnLoadEnd, msSinceBootOnMainStart,
            msSinceBootOnMatchStart, msSinceBootOnSessionEnd)
        dbManager.saveSessionInRemote(session)
        analyticsManager.logSession(session)
    }

    // Local + remote which need to be split into smaller bits
    override fun recoverRealmDb(group: Constants.GROUP, callback: DataCallback) {
        dbManager.recoverLocalDb(deviceId, group, callback)
    }

    override fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>): Boolean =
        dbManager.saveIdentification(probe, matchSize, matches, sessionId)

    override fun saveRefusalForm(refusalForm: RefusalForm): Boolean =
        dbManager.saveRefusalForm(refusalForm, sessionId)

    override fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        dbManager.saveVerification(probe, patientId, match, sessionId, guidExistsResult)
    }

    // Secure Data

    override fun getApiKeyOrEmpty(): String =
        getApiKeyOr("")
}
