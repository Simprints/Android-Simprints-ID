
package com.simprints.id.data

import android.content.Context
import android.os.Build
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.authListener.AuthListener
import com.simprints.id.data.db.remote.connectionListener.ConnectionListener
import com.simprints.id.data.models.Session
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.sessionParameters.SessionParameters
import com.simprints.id.libdata.DataCallback
import com.simprints.id.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Emitter

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
        }

    override fun logAlert(alertType: ALERT_TYPE) =
        analyticsManager.logAlert(alertType.name, getSignedInProjectIdOrEmpty(), moduleId, userId, deviceId)

    override fun logUserProperties() =
        analyticsManager.logUserProperties(userId, getSignedInProjectIdOrEmpty(), moduleId, deviceId)

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

    // DbManager call interception for populating arguments
    // Lifecycle
    override fun initialiseDb() {
        dbManager.registerRemoteConnectionListener(connectionStateLogger)
        dbManager.registerRemoteAuthListener(authStateLogger)
        dbManager.initialiseDb()
    }

    override fun signOut() {
        dbManager.unregisterRemoteConnectionListener(connectionStateLogger)
        dbManager.unregisterRemoteAuthListener(authStateLogger)
        dbManager.signOut()
    }

    private val connectionStateLogger = object : ConnectionListener {
        override fun onConnection() = logConnectionStateChange(true)
        override fun onDisconnection() = logConnectionStateChange(false)
    }

    private val authStateLogger = object : AuthListener {
        override fun onSignIn() = logAuthStateChange(true)
        override fun onSignOut() = logAuthStateChange(false)
    }

    // Data transfer
    override fun savePerson(person: Person) {
        dbManager.savePerson(fb_Person(person, userId, moduleId), projectId)
    }

    override fun loadPeople(destinationList: MutableList<Person>, group: com.simprints.id.libdata.tools.Constants.GROUP, callback: DataCallback?) {
        dbManager.loadPeople(destinationList, group, userId, moduleId, callback)
    }

    override fun getPeopleCount(group: com.simprints.id.libdata.tools.Constants.GROUP): Long =
        dbManager.getPeopleCount(group, userId, moduleId)

    override fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>) {
        dbManager.saveIdentification(probe, projectId, userId, deviceId, moduleId, matchSize, matches, sessionId)
    }

    override fun updateIdentification(projectId: String, selectedGuid: String) {
        dbManager.updateIdentificationInRemote(projectId, selectedGuid, deviceId, sessionId)
    }

    override fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        dbManager.saveVerification(probe, projectId, userId, deviceId, moduleId, patientId, match, sessionId, guidExistsResult)
    }

    override fun saveRefusalForm(refusalForm: RefusalForm) {
        dbManager.saveRefusalForm(refusalForm, projectId, userId, sessionId)
    }

    override fun saveSession() {
        val session = Session(sessionId, androidSdkVersion, deviceModel, deviceId, appVersionName,
            libVersionName, calloutAction.toString(), getSignedInProjectIdOrEmpty(), moduleId, userId,
            patientId, callingPackage, metadata, resultFormat, macAddress, scannerId,
            hardwareVersion.toInt(), location.latitude, location.longitude,
            msSinceBootOnSessionStart, msSinceBootOnLoadEnd, msSinceBootOnMainStart,
            msSinceBootOnMatchStart, msSinceBootOnSessionEnd)
        dbManager.saveSessionInRemote(session)
        analyticsManager.logSession(session)
    }

    override fun syncGlobal(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        dbManager.syncGlobal(projectId, isInterrupted, emitter)
    }

    override fun syncUser(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        dbManager.syncUser(projectId, userId, isInterrupted, emitter)
    }

    override fun recoverRealmDb(group: com.simprints.id.libdata.tools.Constants.GROUP, callback: DataCallback) {
        dbManager.recoverLocalDb(deviceId, userId, deviceId, moduleId, group, callback)
    }
}
