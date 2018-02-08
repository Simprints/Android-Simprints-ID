package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.exceptions.unsafe.UninitializedDataManagerError
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libdata.DataCallback
import com.simprints.libdata.DatabaseContext
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Emitter
import timber.log.Timber

class DbManagerImpl(private val localDbManager: LocalDbManager,
                    private val remoteDbManager: RemoteDbManager) :
    DbManager,
    LocalDbManager by localDbManager,
    RemoteDbManager by remoteDbManager {

    // Local + remote which need to be split into smaller bits

    // TODO
    override fun recoverLocalDb(deviceId: String, group: Constants.GROUP, callback: DataCallback) {
        localDbManager.recoverLocalDbGetFile(deviceId, group, callback)
        remoteDbManager.recoverLocalDbSendToRemote(deviceId, group, callback)
    }

    // TODO
    override fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>, sessionId: String): Boolean =
        localDbManager.saveIdentificationInLocal(probe, matchSize, matches, sessionId) &&
            remoteDbManager.saveIdentificationInRemote(probe, matchSize, matches, sessionId)

    // TODO
    override fun savePerson(person: Person): Boolean =
        localDbManager.savePersonInLocal(person) &&
            remoteDbManager.savePersonInRemote(person)

    // TODO
    override fun saveRefusalForm(refusalForm: RefusalForm, sessionId: String): Boolean =
        localDbManager.saveRefusalFormInLocal(refusalForm, sessionId) &&
            remoteDbManager.saveRefusalFormInRemote(refusalForm, sessionId)

    // TODO
    override fun saveVerification(probe: Person, patientId: String, match: Verification?, sessionId: String,
                                  guidExistsResult: VERIFY_GUID_EXISTS_RESULT): Boolean =
        localDbManager.saveVerificationInLocal(probe, patientId, match, sessionId, guidExistsResult) &&
            remoteDbManager.saveVerificationInRemote(probe, patientId, match, sessionId, guidExistsResult)

    // TODO
    override fun loadPerson(destinationList: MutableList<Person>, guid: String, callback: DataCallback) {
        localDbManager.loadPersonFromLocal(destinationList, guid, callback)
        remoteDbManager.loadPersonFromRemote(destinationList, guid, callback)
    }

    // Local + remote + api which need to be split into smaller bits

//    private var dbContext: DatabaseContext? = null
//        set(value) = synchronized(this) {
//            Timber.d("DataManagerImpl: set dbContext = $value")
//            if (field != null) {
//                unregisterRemoteConnectionListener(connectionStateLogger)
//                unregisterRemoteAuthListener(authStateLogger)
//            }
//            field = value
//            if (value != null) {
//                registerRemoteConnectionListener(connectionStateLogger)
//                registerRemoteAuthListener(authStateLogger)
//            }
//        }
//
//    private fun getDbContextOrErr(): DatabaseContext =
//        dbContext ?: throw UninitializedDataManagerError()

    // TODO
    override fun isInitialized(): Boolean =
        localDbManager.isLocalDbInitialized() &&
            remoteDbManager.isRemoteDbInitialized()

    override fun initialize(callback: DataCallback) {

//        val tentativeDbContext = DatabaseContext(apiKey, userId, moduleId, deviceId, context, BuildConfig.GCP_PROJECT)
//
//        tentativeDbContext.initDatabase(object : DataCallback {
//            override fun onSuccess() {
//                dbContext = tentativeDbContext
//                callback.onSuccess()
//            }
//
//            override fun onFailure(error: DATA_ERROR) {
//                tentativeDbContext.destroy()
//                callback.onFailure(error)
//            }
//        })

        initializeLocalDb(callback)
        initializeRemoteDb(callback)
    }

    // TODO
    override fun signIn() {
        remoteDbManager.signInToRemote()
        val localDbKey = remoteDbManager.getLocalDbKeyFromRemote()
        localDbManager.signInToLocal(localDbKey)
    }

    override fun syncGlobal(isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        TODO()
        // naiveSyncManager.syncGlobal(isInterrupted, emitter)
    }

    override fun syncUser(userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        TODO()
        // naiveSyncManager.syncUser(userId, isInterrupted, emitter)
    }

    // TODO
    override fun finish() {
        localDbManager.finishLocalDb()
        remoteDbManager.finishRemoteDb()
    }
}
