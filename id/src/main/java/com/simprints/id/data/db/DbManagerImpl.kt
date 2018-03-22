package com.simprints.id.data.db

import com.simprints.id.data.db.dbRecovery.LocalDbRecovererImpl
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.NaiveSync
import com.simprints.id.domain.Constants
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.session.Session
import com.simprints.id.tools.JsonHelper
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class DbManagerImpl(private val localDbManager: LocalDbManager,
                    private val remoteDbManager: RemoteDbManager) :
    DbManager,
    LocalDbManager by localDbManager,
    RemoteDbManager by remoteDbManager {

    // Lifecycle

    override fun initialiseDb() {
        remoteDbManager.initialiseRemoteDb()
    }

    override fun getLocalKeyAndSignInToLocal(projectId: String): Completable =
        remoteDbManager
            .getLocalDbKeyFromRemote(projectId)
            .signInToLocal(projectId)

    override fun signIn(projectId: String, tokens: Tokens): Completable =
        remoteDbManager
            .signInToRemoteDb(tokens)
            .andThen(getLocalKeyAndSignInToLocal(projectId))

    private fun Single<out LocalDbKey>.signInToLocal(projectId: String): Completable =
        flatMapCompletable { key ->
            localDbManager.signInToLocal(projectId, key)
        }

    override fun signOut() {
        localDbManager.signOutOfLocal()
        remoteDbManager.signOutOfRemoteDb()
    }

    override fun isDbInitialised(): Boolean =
        remoteDbManager.isRemoteDbInitialized()

    // Data transfer
    override fun savePerson(fbPerson: fb_Person): Completable =
        uploadPersonAndDownloadAgain(fbPerson)
            .updatePersonInLocal()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun Single<out fb_Person>.updatePersonInLocal(): Completable =
        flatMapCompletable {
            localDbManager.updatePersonInLocal(it)
        }

    private fun uploadPersonAndDownloadAgain(fbPerson: fb_Person): Single<fb_Person> =
        remoteDbManager
            .uploadPeopleBatch(arrayListOf(fbPerson))
            .andThen(remoteDbManager.downloadPatient(fbPerson.patientId))

    override fun loadPerson(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback) {
        localDbManager.loadPersonFromLocal(destinationList, guid, callback)
        if (destinationList.size == 0) remoteDbManager.loadPersonFromRemote(destinationList, projectId, guid, callback)
    }

    override fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?) {
        localDbManager.loadPeopleFromLocal(destinationList, group, userId, moduleId, callback)
    }

    override fun getPeopleCount(personId: String?,
                                projectId: String?,
                                userId: String?,
                                moduleId: String?,
                                toSync: Boolean?): Int =
        localDbManager.getPeopleCountFromLocal(projectId, personId, userId, moduleId, toSync)

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
        NaiveSync(
            localDbManager,
            remoteDbManager,
            JsonHelper.gson).sync(interrupted, parameters)

    override fun recoverLocalDb(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP, callback: DataCallback) {
        val firebaseManager = remoteDbManager as FirebaseManager
        val realmManager = localDbManager as RealmDbManager
        LocalDbRecovererImpl(realmManager, firebaseManager, projectId, userId, androidId, moduleId, group, callback).recoverDb()
    }

    companion object {
        private const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5L
    }
}
