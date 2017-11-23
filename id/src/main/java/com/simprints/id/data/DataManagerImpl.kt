package com.simprints.id.data

import android.content.Context
import android.os.Build
import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.FingerIdentifier

class DataManagerImpl(private val context: Context,
                      private val preferencesManager: PreferencesManager,
                      private val localDbManager: LocalDbManager,
                      private val remoteDbManager: RemoteDbManager,
                      private val apiManager: ApiManager,
                      private val analyticsManager: AnalyticsManager): DataManager {

    override val androidSdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override val deviceId: String
        get() = context.deviceId

    override val appVersionName: String
        get() = context.packageVersionName

    override var nudgeMode: Boolean = preferencesManager.nudgeMode
    override var consent: Boolean = preferencesManager.consent
    override var qualityThreshold: Int = preferencesManager.qualityThreshold
    override var returnIdCount: Int = preferencesManager.returnIdCount
    override var language: String = preferencesManager.language
    override var languagePosition: Int = preferencesManager.languagePosition
    override var matcherType: Int = preferencesManager.matcherType
    override var timeoutS: Int = preferencesManager.timeoutS
    override var appKey: String = preferencesManager.appKey
    override var syncGroup: Constants.GROUP = preferencesManager.syncGroup
    override var matchGroup: Constants.GROUP = preferencesManager.matchGroup
    override var vibrateMode: Boolean = preferencesManager.vibrateMode
    override var lastUserId: String = preferencesManager.lastUserId
    override var matchingEndWaitTimeS: Int = preferencesManager.matchingEndWaitTimeS
    override var fingerStatusPersist: Boolean = preferencesManager.fingerStatusPersist
    override fun getFingerStatus(fingerIdentifier: FingerIdentifier): Boolean = preferencesManager.getFingerStatus(fingerIdentifier)
    override fun setFingerStatus(fingerIdentifier: FingerIdentifier, show: Boolean) = preferencesManager.setFingerStatus(fingerIdentifier, show)
}