package com.simprints.id.data.db

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.models.Session
import com.simprints.id.secure.models.Tokens
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libdata.DataCallback
import com.simprints.libdata.NaiveSyncManager
import com.simprints.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.libdata.models.firebase.fb_Person
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Emitter
import io.reactivex.Single

class DbManagerImpl(private val localDbManager: LocalDbManager,
                    private var remoteDbManager: RemoteDbManager) :
    DbManager,
    LocalDbManager by localDbManager,
    RemoteDbManager by remoteDbManager {

    // Lifecycle

    override fun initialiseDb(projectId: String): Single<Unit> {
        remoteDbManager.initialiseRemoteDb()

        return if (projectId.isNotEmpty()) {
            getLocalKeyAndSignInToLocal(projectId)
        } else {
            Single.just(Unit)
        }
    }

    override fun getLocalKeyAndSignInToLocal(projectId: String): Single<Unit> =
        remoteDbManager
            .getLocalDbKeyFromRemote(projectId)
            .signInToLocal(projectId)

    override fun signIn(projectId: String, token: Tokens): Single<Unit> =
        remoteDbManager
            .signInToRemoteDb(token)
            .flatMap {
                getLocalKeyAndSignInToLocal(projectId)
            }

    private fun Single<out String>.signInToLocal(projectId: String): Single<Unit> =
        flatMap { key ->
            localDbManager.signInToLocal(projectId, key)
        }

    override fun signOut() {
        // TODO
        localDbManager.signOutOfLocal()
        remoteDbManager.signOutOfRemoteDb()
    }

    override fun isDbInitialised(): Boolean =
        // TODO
        remoteDbManager.isRemoteDbInitialized()

    // Data transfer

    override fun savePerson(fbPerson: fb_Person, projectId: String) {
        localDbManager.savePersonInLocal(fbPerson)
        remoteDbManager.savePersonInRemote(fbPerson, projectId)
    }

    override fun loadPerson(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback) {
        localDbManager.loadPersonFromLocal(destinationList, guid, callback)
        if (destinationList.size == 0) remoteDbManager.loadPersonFromRemote(destinationList, projectId, guid, callback)
    }

    override fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?) {
        localDbManager.loadPeopleFromLocal(destinationList, group, userId, moduleId, callback)
    }

    override fun getPeopleCount(group: Constants.GROUP, userId: String, moduleId: String): Long =
        localDbManager.getPeopleCountFromLocal(group, userId, moduleId)

    override fun saveIdentification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String) {
        remoteDbManager.saveIdentificationInRemote(probe, projectId, userId, androidId, moduleId, matchSize, matches, sessionId)
    }

    override fun saveVerification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        remoteDbManager.saveVerificationInRemote(probe, projectId, userId, androidId, moduleId, patientId, match, sessionId, guidExistsResult)
    }

    override fun saveSession(session: Session) {
        remoteDbManager.saveSessionInRemote(session)
    }

    override fun saveRefusalForm(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String) {
        remoteDbManager.saveRefusalFormInRemote(refusalForm, projectId, userId, sessionId)
    }

    override fun syncGlobal(legacyApiKey: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        getSyncManager(legacyApiKey).syncGlobal(isInterrupted, emitter)
    }

    override fun syncUser(legacyApiKey: String, userId: String, isInterrupted: () -> Boolean, emitter: Emitter<Progress>) {
        getSyncManager(legacyApiKey).syncUser(userId, isInterrupted, emitter)
    }

    fun getSyncManager(legacyApiKey: String): NaiveSyncManager =
        // if localDbManager.realmConfig is null, the user is not signed in and we should not be here,
        // TODO: that is temporary, we will fix in the migration firestore
        NaiveSyncManager(remoteDbManager.getFirebaseLegacyApp(), legacyApiKey, localDbManager.realmConfig!!)

    override fun recoverLocalDb(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP, callback: DataCallback) {
        val firebaseManager = remoteDbManager as FirebaseManager
        LocalDbRecovererImpl(firebaseManager, projectId, userId, androidId, moduleId, group, callback)
    }
}
