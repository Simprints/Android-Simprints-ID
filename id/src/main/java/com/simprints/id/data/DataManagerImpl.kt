package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.domain.Constants
import com.simprints.id.session.Session
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import java.util.*

class DataManagerImpl(val preferencesManager: PreferencesManager,
                      val dbManager: DbManager,
                      val loginInfoManager: LoginInfoManager,
                      override val analytics: AnalyticsManager)
    : DataManager,
    PreferencesManager by preferencesManager,
    DbManager by dbManager,
    LoginInfoManager by loginInfoManager {

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
        val session = Session(
            preferencesManager.sessionId,
            preferencesManager.androidSdkVersion,
            preferencesManager.deviceModel,
            preferencesManager.deviceId,
            preferencesManager.appVersionName,
            preferencesManager.libVersionName,
            preferencesManager.calloutAction.toString(),
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            preferencesManager.moduleId,
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.patientId,
            preferencesManager.callingPackage,
            preferencesManager.metadata,
            preferencesManager.resultFormat,
            preferencesManager.macAddress,
            preferencesManager.scannerId,
            preferencesManager.hardwareVersion.toInt(),
            preferencesManager.location.latitude,
            preferencesManager.location.longitude,
            preferencesManager.msSinceBootOnSessionStart,
            preferencesManager.msSinceBootOnLoadEnd,
            preferencesManager.msSinceBootOnMainStart,
            preferencesManager.msSinceBootOnMatchStart,
            preferencesManager.msSinceBootOnSessionEnd)
        dbManager.saveSessionInRemote(session)
        analytics.logSession(session)
    }

    override fun recoverRealmDb(group: Constants.GROUP): Completable {
        return dbManager.recoverLocalDb(getSignedInProjectIdOrEmpty(), getSignedInUserIdOrEmpty(), deviceId, moduleId, group)
    }
}
