package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.session.Session

class DataManagerImpl(val preferencesManager: PreferencesManager,
                      override val loginInfo: LoginInfoManager,
                      override val db: DbManager,
                      override val analytics: AnalyticsManager)
    : DataManager,
    PreferencesManager by preferencesManager {

    override fun saveSession() {
        val session = Session(
            preferencesManager.sessionId,
            preferencesManager.androidSdkVersion,
            preferencesManager.deviceModel,
            preferencesManager.deviceId,
            preferencesManager.appVersionName,
            preferencesManager.libVersionName,
            preferencesManager.calloutAction.toString(),
            loginInfo.getSignedInProjectIdOrEmpty(),
            preferencesManager.moduleId,
            loginInfo.getSignedInUserIdOrEmpty(),
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
        db.saveSessionInRemote(session)
        analytics.logSession(session)
    }
}
