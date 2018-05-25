package com.simprints.id.data

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Constants
import com.simprints.id.session.Session
import com.simprints.id.session.sessionParameters.SessionParameters
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import java.util.*

class DataManagerImpl(private val context: Context,
                      private val preferencesManager: PreferencesManager,
                      private val dbManager: DbManager,
                      private val analyticsManager: AnalyticsManager,
                      private val loginInfoManager: LoginInfoManager)
    : DataManager,
    PreferencesManager by preferencesManager,
    AnalyticsManager by analyticsManager,
    DbManager by dbManager,
    LoginInfoManager by loginInfoManager {

    override val androidSdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override var sessionParameters: SessionParameters
        get() = preferencesManager.sessionParameters
        set(value) {
            preferencesManager.sessionParameters = value
        }

    override fun logAlert(alertType: ALERT_TYPE) =
        analyticsManager.logAlert(alertType.name, getSignedInProjectIdOrEmpty(), moduleId, getSignedInUserIdOrEmpty(), deviceId)

    override fun logUserProperties() =
        analyticsManager.logUserProperties(getSignedInUserIdOrEmpty(), getSignedInProjectIdOrEmpty(), moduleId, deviceId)

    override fun logScannerProperties() =
        analyticsManager.logScannerProperties(macAddress, scannerId)

    override fun logGuidSelectionService(apiKey: String, sessionId: String, selectedGuid: String,
                                         callbackSent: Boolean) =
        analyticsManager.logGuidSelectionService(apiKey, sessionId, selectedGuid, callbackSent,
            deviceId)

    override fun logConnectionStateChange(connected: Boolean) =
        analyticsManager.logConnectionStateChange(connected, getSignedInProjectIdOrEmpty(), deviceId, sessionId)

    override fun logAuthStateChange(authenticated: Boolean) =
        analyticsManager.logAuthStateChange(authenticated, getSignedInProjectIdOrEmpty(), deviceId, sessionId)

    // Data transfer
    override fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>) {
        preferencesManager.lastIdentificationDate = Date()
        dbManager.saveIdentification(probe, getSignedInProjectIdOrEmpty(), getSignedInUserIdOrEmpty(), deviceId, moduleId, matchSize, matches, sessionId)
    }

    override fun updateIdentification(projectId: String, selectedGuid: String) =
        dbManager.updateIdentificationInRemote(projectId, selectedGuid, deviceId, sessionId)

    override fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        preferencesManager.lastVerificationDate = Date()
        dbManager.saveVerification(probe, getSignedInProjectIdOrEmpty(), getSignedInUserIdOrEmpty(), deviceId, moduleId, patientId, match, sessionId, guidExistsResult)
    }

    override fun saveRefusalForm(refusalForm: RefusalForm) =
        dbManager.saveRefusalForm(refusalForm, getSignedInProjectIdOrEmpty(), getSignedInUserIdOrEmpty(), sessionId)

    override fun saveSession() {
        val session = Session(sessionId, androidSdkVersion, deviceModel, deviceId, appVersionName,
            libVersionName, calloutAction.toString(), getSignedInProjectIdOrEmpty(), moduleId, getSignedInUserIdOrEmpty(),
            patientId, callingPackage, metadata, resultFormat, macAddress, scannerId,
            hardwareVersion.toInt(), location.latitude, location.longitude,
            msSinceBootOnSessionStart, msSinceBootOnLoadEnd, msSinceBootOnMainStart,
            msSinceBootOnMatchStart, msSinceBootOnSessionEnd)
        dbManager.saveSessionInRemote(session)
        analyticsManager.logSession(session)
    }

    override fun recoverRealmDb(group: Constants.GROUP): Completable {
        return dbManager.recoverLocalDb(getSignedInProjectIdOrEmpty(), getSignedInUserIdOrEmpty(), deviceId, moduleId, group)
    }
}
