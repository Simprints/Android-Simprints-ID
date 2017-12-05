package com.simprints.id.data

import android.content.Context
import android.os.Build
import com.simprints.id.BuildConfig
import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.prefs.PreferencesManager
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
import timber.log.Timber


class DataManagerImpl(private val context: Context,
                      private val preferencesManager: PreferencesManager,
                      private val localDbManager: LocalDbManager,
                      private val remoteDbManager: RemoteDbManager,
                      private val apiManager: ApiManager,
                      private val analyticsManager: AnalyticsManager)
    : DataManager,
        PreferencesManager by preferencesManager,
        AnalyticsManager by analyticsManager,
        LocalDbManager by localDbManager,
        RemoteDbManager by remoteDbManager,
        ApiManager by apiManager {

    override val androidSdkVersion: Int
        get() = Build.VERSION.SDK_INT

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override val deviceId: String
        get() = context.deviceId

    override val appVersionName: String
        get() = context.packageVersionName


    override fun logAlert(alertType: ALERT_TYPE) =
            analyticsManager.logAlert(alertType.name, apiKey, moduleId, userId, deviceId)

    override fun logUserProperties() =
            analyticsManager.logUserProperties(userId, apiKey, moduleId, deviceId)

    override fun logLogin() =
            analyticsManager.logLogin(calloutType)

    override fun logGuidSelectionService(selectedGuid: String, callbackSent: Boolean) =
            analyticsManager.logGuidSelectionService(selectedGuid, callbackSent, apiKey, deviceId,
                    sessionId)

    override fun logConnectionStateChange(connected: Boolean) =
            analyticsManager.logConnectionStateChange(connected, apiKey, deviceId, sessionId)

    override fun logAuthStateChange(authenticated: Boolean) =
            analyticsManager.logAuthStateChange(authenticated, apiKey, deviceId, sessionId)

    private val connectionStateLogger = object: ConnectionListener {
        override fun onConnection() = logConnectionStateChange(true)
        override fun onDisconnection() = logConnectionStateChange(false)
    }

    private val authStateLogger = object: AuthListener {
        override fun onSignIn() = logAuthStateChange(true)
        override fun onSignOut() = logAuthStateChange(false)
    }

    // Remote only

    override fun isConnected(): Boolean =
            dbContext.callSafelyOrLogNonFatalExceptionOn("isConnected",
                    { remoteDbManager.isConnected(it) },
                    false)

    override fun registerAuthListener(authListener: AuthListener) =
            dbContext.callSafelyOrLogNonFatalExceptionOn("registerAuthListener")
            { remoteDbManager.registerAuthListener(it, authListener) }

    override fun registerConnectionListener(connectionListener: ConnectionListener) =
            dbContext.callSafelyOrLogNonFatalExceptionOn("registerConnectionListener")
            { remoteDbManager.registerConnectionListener(it, connectionListener) }

    override fun unregisterAuthListener(authListener: AuthListener) =
            dbContext.callSafelyOrLogNonFatalExceptionOn("unregisterAuthListener")
            { remoteDbManager.unregisterAuthListener(it, authListener) }

    override fun unregisterConnectionListener(connectionListener: ConnectionListener) =
            dbContext.callSafelyOrLogNonFatalExceptionOn("unregisterConnectionListener")
            { remoteDbManager.unregisterConnectionListener(it, connectionListener) }

    override fun updateIdentification(selectedGuid: String): Boolean =
        remoteDbManager.updateIdentification(apiKey, selectedGuid, deviceId, sessionId)

    // Local only


    override fun getPeopleCount(group: Constants.GROUP): Long =
            dbContext.callSafelyOrLogNonFatalExceptionOn("getPeopleCount",
                    { localDbManager.getPeopleCount(it, group) },
                    -1)

    override fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP,
                            callback: DataCallback?) {
        dbContext.callSafelyOrLogNonFatalExceptionOn("loadPeople",
                { localDbManager.loadPeople(it, destinationList, group, callback) })
    }

    // Local + remote which need to be split into smaller bits


    override fun recoverRealmDb(group: Constants.GROUP, callback: DataCallback) {
        val filename = "${deviceId}_${System.currentTimeMillis()}.json"
        dbContext.callSafelyOrLogNonFatalExceptionOn("recoverRealmDb",
                { it.recoverRealmDb(filename, deviceId, group, callback) })
    }

    override fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>)
            : Boolean =
            dbContext.callSafelyOrLogNonFatalExceptionOn("saveIdentification",
                    { it.saveIdentification(probe, matchSize, matches, sessionId) },
                    false)

    override fun savePerson(person: Person): Boolean =
            dbContext.callSafelyOrLogNonFatalExceptionOn("savePerson",
                    { it.savePerson(person) },
                    false)

    override fun saveRefusalForm(refusalForm: RefusalForm): Boolean =
            dbContext.callSafelyOrLogNonFatalExceptionOn("saveRefusalForm",
                    { it.saveRefusalForm(refusalForm, sessionId) },
                    false)

    override fun saveVerification(probe: Person, match: Verification?,
                                  guidExistsResult: VERIFY_GUID_EXISTS_RESULT): Boolean =
            dbContext.callSafelyOrLogNonFatalExceptionOn("saveVerification",
                    { it.saveVerification(probe, patientId, match, sessionId, guidExistsResult) },
                    false)

    override fun loadPerson(destinationList: MutableList<Person>, guid: String, callback: DataCallback) =
            dbContext.callSafelyOrLogNonFatalExceptionOn("loadPerson")
            { it.loadPerson(destinationList, guid, callback) }


    // Local + remote + api which need to be split into smaller bits

    private var dbContext: DatabaseContext? = null
        set(value) {
            Timber.d("DataManagerImpl: set dbContext = $dbContext")
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

    override fun isInitialized(): Boolean =
            dbContext != null

    override fun initialize(callback: DataCallback?) {
        val tentativeDbContext = DatabaseContext(apiKey, userId, moduleId, deviceId, context, BuildConfig.FIREBASE_PROJECT)

        tentativeDbContext.initDatabase(object : DataCallback {
            override fun onSuccess() {
                dbContext = tentativeDbContext
                callback?.onSuccess()
            }

            override fun onFailure(error: DATA_ERROR) {
                tentativeDbContext.destroy()
                callback?.onFailure(error)
            }
        })
    }

    override fun signIn(callback: DataCallback?) =
            dbContext.callSafelyOrLogNonFatalExceptionOn("signIn")
            { it.signIn(callback) }

    override fun finish() {
        dbContext.callSafelyOrLogNonFatalExceptionOn("finish")
        { it.destroy() }
        dbContext = null
    }

    /**
     * Performs the specified call using this database context if it is non null.
     * Else, log a non fatal exception.
     */
    private fun <T: Any?> DatabaseContext?.callSafelyOrLogNonFatalExceptionOn(
            methodName: String, call: (DatabaseContext) -> T, onFailureReturn: T): T {
        return if (this == null) {
            logNonFatalException("Cannot $methodName because dbContext is null")
            onFailureReturn
        } else {
            call(this)
        }
    }

    private fun DatabaseContext?.callSafelyOrLogNonFatalExceptionOn(methodName: String,
                                                                    call: (DatabaseContext) -> Unit) =
            callSafelyOrLogNonFatalExceptionOn(methodName, call, Unit)


}
