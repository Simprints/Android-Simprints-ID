package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.session.Session

class DataManagerImpl(override val preferences: PreferencesManager,
                      override val loginInfo: LoginInfoManager,
                      override val secure: SecureDataManager,
                      override val analytics: AnalyticsManager,
                      override val db: DbManager) : DataManager {

    override fun saveSession() {
        val session = Session(
            preferences.sessionId,
            preferences.androidSdkVersion,
            preferences.deviceModel,
            preferences.deviceId,
            preferences.appVersionName,
            preferences.libVersionName,
            preferences.calloutAction.toString(),
            loginInfo.getSignedInProjectIdOrEmpty(),
            preferences.moduleId,
            loginInfo.getSignedInUserIdOrEmpty(),
            preferences.patientId,
            preferences.callingPackage,
            preferences.metadata,
            preferences.resultFormat,
            preferences.macAddress,
            preferences.scannerId,
            preferences.hardwareVersion.toInt(),
            preferences.location.latitude,
            preferences.location.longitude,
            preferences.msSinceBootOnSessionStart,
            preferences.msSinceBootOnLoadEnd,
            preferences.msSinceBootOnMainStart,
            preferences.msSinceBootOnMatchStart,
            preferences.msSinceBootOnSessionEnd)
        db.remote.saveSessionInRemote(session)
        analytics.logSession(session)
    }
}
