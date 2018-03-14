package com.simprints.id.data.db

import com.simprints.id.data.db.dbRecovery.LocalDbRecovererImpl
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.NaiveSyncManager
import com.simprints.id.data.models.Session
import com.simprints.id.libdata.DataCallback
import com.simprints.id.libdata.models.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.libdata.models.firebase.fb_Person
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.libcommon.Person
import com.simprints.libcommon.Progress
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Observable
import io.reactivex.Single

class DbManagerImpl(private val localDbManager: LocalDbManager,
                    private val remoteDbManager: RemoteDbManager) :
    DbManager,
    LocalDbManager by localDbManager,
    RemoteDbManager by remoteDbManager {

    // Lifecycle

    override fun initialiseDb() {
        remoteDbManager.initialiseRemoteDb()
    }

    override fun getLocalKeyAndSignInToLocal(projectId: String): Single<Unit> =
        remoteDbManager
            .getLocalDbKeyFromRemote(projectId)
            .signInToLocal(projectId)

    override fun signIn(projectId: String, tokens: Tokens): Single<Unit> =
        remoteDbManager
            .signInToRemoteDb(tokens)
            .flatMap {
                getLocalKeyAndSignInToLocal(projectId)
            }

    private fun Single<out LocalDbKey>.signInToLocal(projectId: String): Single<Unit> =
        flatMap { key ->
            localDbManager.signInToLocal(projectId, key)
        }

    override fun signOut() {
        localDbManager.signOutOfLocal()
        remoteDbManager.signOutOfRemoteDb()
    }

    override fun isDbInitialised(): Boolean =
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

    override fun loadPeople(destinationList: MutableList<Person>, group: com.simprints.id.libdata.tools.Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?) {
        localDbManager.loadPeopleFromLocal(destinationList, group, userId, moduleId, callback)
    }

    override fun getPeopleCount(group: com.simprints.id.libdata.tools.Constants.GROUP, userId: String, moduleId: String): Long =
        localDbManager.getPeopleCountFromLocal(group, userId, moduleId)

    override fun saveIdentification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, matchSize: Int, matches: List<Identification>, sessionId: String) {
        remoteDbManager.saveIdentificationInRemote(probe, projectId, userId, moduleId, androidId, matchSize, matches, sessionId)
    }

    override fun saveVerification(probe: Person, projectId: String, userId: String, androidId: String, moduleId: String, patientId: String, match: Verification?, sessionId: String, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        remoteDbManager.saveVerificationInRemote(probe, projectId, userId, moduleId, androidId, patientId, match, sessionId, guidExistsResult)
    }

    override fun saveSession(session: Session) {
        remoteDbManager.saveSessionInRemote(session)
    }

    override fun saveRefusalForm(refusalForm: RefusalForm, projectId: String, userId: String, sessionId: String) {
        remoteDbManager.saveRefusalFormInRemote(refusalForm, projectId, userId, sessionId)
    }

    override fun sync(parameters: SyncTaskParameters, interrupted: () -> Boolean): Observable<Progress> =
        getSyncManager().sync(parameters, interrupted)

    private fun getSyncManager(): NaiveSyncManager =
        NaiveSyncManager(remoteDbManager.getFirebaseLegacyApp(), localDbManager.getValidRealmConfig(), localDbManager)

    override fun recoverLocalDb(projectId: String, userId: String, androidId: String, moduleId: String, group: com.simprints.id.libdata.tools.Constants.GROUP, callback: DataCallback) {
        val firebaseManager = remoteDbManager as FirebaseManager
        val realmManager = localDbManager as RealmDbManager
        LocalDbRecovererImpl(realmManager, firebaseManager, projectId, userId, androidId, moduleId, group, callback).recoverDb()
    }
}
