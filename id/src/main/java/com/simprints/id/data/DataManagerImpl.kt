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

class DataManagerImpl(private val context: Context,
                      private val preferencesManager: PreferencesManager,
                      private val localDbManager: LocalDbManager,
                      private val remoteDbManager: RemoteDbManager,
                      private val apiManager: ApiManager,
                      private val analyticsManager: AnalyticsManager): DataManager,
        PreferencesManager by preferencesManager {

    override val androidSdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override val deviceId: String
        get() = context.deviceId

    override val appVersionName: String
        get() = context.packageVersionName

    // Session parameters

//    override var apiKey: String
//        get() = preferencesManager.apiKey
//        set(value) { preferencesManager.apiKey = value }
//
//    override var callout: Callout
//        get() = preferencesManager.callout
//        set(value) { preferencesManager.callout = value }
//
//    override var moduleId: String
//        get() = preferencesManager.moduleId
//        set(value) { preferencesManager.moduleId = value }
//
//    override var userId: String
//        get() = preferencesManager.userId
//        set(value) { preferencesManager.userId = value }
//
//    override var patientId: String
//        get() = preferencesManager.patientId
//        set(value) { preferencesManager.patientId = value }
//
//    override var callingPackage: String
//        get() = preferencesManager.callingPackage
//        set(value) { preferencesManager.callingPackage = value }
//
//    override var metadata: String
//        get() = preferencesManager.metadata
//        set(value) { preferencesManager.metadata = value }
//
//    override var resultFormat: String
//        get() = preferencesManager.resultFormat
//        set(value) { preferencesManager.resultFormat = value }
//
//    // Settings
//
//    override var nudgeMode: Boolean
//        get() = preferencesManager.nudgeMode
//        set(value) { preferencesManager.nudgeMode = value }
//
//    override var consent: Boolean
//        get() = preferencesManager.nudgeMode
//        set(value) { preferencesManager.nudgeMode = value }
//
//    override var qualityThreshold: Int
//        get() = preferencesManager.qualityThreshold
//        set(value) { preferencesManager.qualityThreshold = value }
//
//    override var returnIdCount: Int
//        get() = preferencesManager.returnIdCount
//        set(value) { preferencesManager.returnIdCount = value }
//
//    override var language: String
//        get() = preferencesManager.language
//        set(value) { preferencesManager.language = value }
//
//    override var languagePosition: Int
//        get() = preferencesManager.languagePosition
//        set(value) { preferencesManager.languagePosition = value }
//
//    override var matcherType: Int
//        get() = preferencesManager.matcherType
//        set(value) { preferencesManager.matcherType = value }
//
//    override var timeoutS: Int
//        get() = preferencesManager.timeoutS
//        set(value) { preferencesManager.timeoutS = value }
//
//    override var appKey: String
//        get() = preferencesManager.appKey
//        set(value) { preferencesManager.appKey = value }
//
//    override var syncGroup: Constants.GROUP
//        get() = preferencesManager.syncGroup
//        set(value) { preferencesManager.syncGroup = value }
//
//    override var matchGroup: Constants.GROUP
//        get() = preferencesManager.matchGroup
//        set(value) { preferencesManager.matchGroup = value }
//
//    override var vibrateMode: Boolean
//        get() = preferencesManager.vibrateMode
//        set(value) { preferencesManager.vibrateMode = value }
//
//    override var matchingEndWaitTimeS: Int
//        get() = preferencesManager.matchingEndWaitTimeS
//        set(value) { preferencesManager.matchingEndWaitTimeS = value }
//
//    override var fingerStatusPersist: Boolean
//        get() = preferencesManager.fingerStatusPersist
//        set(value) { preferencesManager.fingerStatusPersist = value }
//
//
//    override fun getFingerStatus(fingerIdentifier: FingerIdentifier): Boolean = preferencesManager.getFingerStatus(fingerIdentifier)
//
//    override fun setFingerStatus(fingerIdentifier: FingerIdentifier, show: Boolean) = preferencesManager.setFingerStatus(fingerIdentifier, show)

}