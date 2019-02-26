package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.session.Session

class DataManagerImpl(val preferencesManager: PreferencesManager,
                      val loginInfoManager: LoginInfoManager,
                      val analyticsManager: AnalyticsManager,
                      val remoteDbManager: RemoteDbManager) : DataManager {

    override fun saveSession() {
        val session = Session(
            preferencesManager.sessionId,
            preferencesManager.androidSdkVersion,
            preferencesManager.deviceModel,
            preferencesManager.deviceId,
            preferencesManager.appVersionName,
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
        remoteDbManager.saveSessionInRemote(session)
        analyticsManager.logSession(session)
    }
}
