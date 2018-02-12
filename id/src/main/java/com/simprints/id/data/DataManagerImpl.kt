package com.simprints.id.data

import android.content.Context
import android.os.Build
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.models.Session
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.sessionParameters.SessionParameters
import com.simprints.id.exceptions.unsafe.ApiKeyNotFoundError
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.packageVersionName
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libdata.*
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Emitter
import timber.log.Timber


class DataManagerImpl(private val context: Context,
                      private val preferencesManager: PreferencesManager,
                      private val localDbManager: LocalDbManager,
                      private val remoteDbManager: RemoteDbManager,
                      private val apiManager: ApiManager,
                      private val analyticsManager: AnalyticsManager,
                      private val secureDataManager: SecureDataManager)
    : DataManager,
    PreferencesManager by preferencesManager,
    AnalyticsManager by analyticsManager,
    LocalDbManager by localDbManager,
    RemoteDbManager by remoteDbManager,
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

    private val connectionStateLogger = object : ConnectionListener {
        override fun onConnection() = logConnectionStateChange(true)
        override fun onDisconnection() = logConnectionStateChange(false)
    }

    private val authStateLogger = object : AuthListener {
        override fun onSignIn() = logAuthStateChange(true)
        override fun onSignOut() = logAuthStateChange(false)
    }

    // Remote only

    @Throws(UninitializedDataManagerError::class)
    override fun isConnected(): Boolean =
        remoteDbManager.isConnected(getDbContextOrErr())

    @Throws(UninitializedDataManagerError::class)
    override fun registerAuthListener(authListener: AuthListener) =
        remoteDbManager.registerAuthListener(getDbContextOrErr(), authListener)

    @Throws(UninitializedDataManagerError::class)
    override fun registerConnectionListener(connectionListener: ConnectionListener) =
        remoteDbManager.registerConnectionListener(getDbContextOrErr(), connectionListener)

    @Throws(UninitializedDataManagerError::class)
    override fun unregisterAuthListener(authListener: AuthListener) =
        remoteDbManager.unregisterAuthListener(getDbContextOrErr(), authListener)

    @Throws(UninitializedDataManagerError::class)
    override fun unregisterConnectionListener(connectionListener: ConnectionListener) =
        remoteDbManager.unregisterConnectionListener(getDbContextOrErr(), connectionListener)

    @Throws(ApiKeyNotFoundError::class)
    override fun updateIdentification(apiKey: String, selectedGuid: String) =
        remoteDbManager.updateIdentification(apiKey, selectedGuid, deviceId, sessionId)

    @Throws(UninitializedDataManagerError::class)
    override fun saveSession() {
        val session = Session(sessionId, androidSdkVersion, deviceModel, deviceId, appVersionName,
            libVersionName, calloutAction.toString(), getSignedInProjectIdOrEmpty(), moduleId, userId,
            patientId, callingPackage, metadata, resultFormat, macAddress, scannerId,
            hardwareVersion.toInt(), location.latitude, location.longitude,
            msSinceBootOnSessionStart, msSinceBootOnLoadEnd, msSinceBootOnMainStart,
            msSinceBootOnMatchStart, msSinceBootOnSessionEnd)
        remoteDbManager.saveSession(getDbContextOrErr(), session)
        analyticsManager.logSession(session)
    }

    // Local only

    @Throws(UninitializedDataManagerError::class)
    override fun getPeopleCount(group: Constants.GROUP): Long =
        localDbManager.getPeopleCount(getDbContextOrErr(), group)

    @Throws(UninitializedDataManagerError::class)
    override fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP,
                            callback: DataCallback?) =
        localDbManager.loadPeople(getDbContextOrErr(), destinationList, group, callback)

    // Local + remote which need to be split into smaller bits

    @Throws(UninitializedDataManagerError::class)
    override fun recoverRealmDb(group: Constants.GROUP, callback: DataCallback) {
        val filename = "${deviceId}_${System.currentTimeMillis()}.json"
        getDbContextOrErr().recoverRealmDb(filename, deviceId, group, callback)
    }

    @Throws(UninitializedDataManagerError::class)
    override fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>)
        : Boolean =
        getDbContextOrErr().saveIdentification(probe, matchSize, matches, sessionId)

    @Throws(UninitializedDataManagerError::class)
    override fun savePerson(person: Person): Boolean =
        getDbContextOrErr().savePerson(person)

    @Throws(UninitializedDataManagerError::class)
    override fun saveRefusalForm(refusalForm: RefusalForm): Boolean =
        getDbContextOrErr().saveRefusalForm(refusalForm, sessionId)

    @Throws(UninitializedDataManagerError::class)
    override fun saveVerification(probe: Person, match: Verification?,
                                  guidExistsResult: VERIFY_GUID_EXISTS_RESULT): Boolean =
        getDbContextOrErr().saveVerification(probe, patientId, match, sessionId, guidExistsResult)

    @Throws(UninitializedDataManagerError::class)
    override fun loadPerson(destinationList: MutableList<Person>, guid: String, callback: DataCallback) =
        getDbContextOrErr().loadPerson(destinationList, guid, callback)

    // Local + remote + api which need to be split into smaller bits

    private var dbContext: DatabaseContext? = null
        set(value) = synchronized(this) {
            Timber.d("DataManagerImpl: set dbContext = $value")
            if (field != null) {
                unregisterConnectionListener(connectionStateLogger)
                unregisterAuthListener(authStateLogger)
            }
            field = value
            if (value != null) {
                registerConnectionListener(connectionStateLogger)
                registerAuthListener(authStateLogger)
            }
        }

    private fun getDbContextOrErr(): DatabaseContext =
        dbContext ?: throw UninitializedDataManagerError()

    override fun isInitialized(): Boolean =
        dbContext != null

    override fun initialize(callback: DataCallback) {

        val apiKey: String
        try {
            apiKey = this.apiKey
        } catch (e: ApiKeyNotFoundError) {
            logError(e)
            callback.onFailure(DATA_ERROR.NOT_FOUND)
            return
        }

        val tentativeDbContext = DatabaseContext(apiKey, userId, moduleId, deviceId, context, BuildConfig.GCP_PROJECT)

        tentativeDbContext.initDatabase(object : DataCallback {
            override fun onSuccess() {
                dbContext = tentativeDbContext
                callback.onSuccess()
            }

            override fun onFailure(error: DATA_ERROR) {
                tentativeDbContext.destroy()
                callback.onFailure(error)
            }
        })
    }

    @Throws(UninitializedDataManagerError::class)
    override fun signIn(callback: DataCallback?) =
        getDbContextOrErr().signIn(callback)

    @Throws(UninitializedDataManagerError::class)
    override fun syncGlobal(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
        getDbContextOrErr().naiveSyncManager.syncGlobal(isInterrupted, emitter)

    @Throws(UninitializedDataManagerError::class)
    override fun syncUser(userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) =
        getDbContextOrErr().naiveSyncManager.syncUser(userId, isInterrupted, emitter)

    @Throws(UninitializedDataManagerError::class)
    override fun finish() {
        getDbContextOrErr().destroy()
        dbContext = null
    }
}
