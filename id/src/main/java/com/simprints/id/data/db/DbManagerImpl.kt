package com.simprints.id.data.db

import com.simprints.id.data.db.dbRecovery.LocalDbRecovererImpl
import com.simprints.id.data.db.local.LocalDbKey
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.local.models.rl_Person
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
import com.simprints.id.tools.json.JsonHelper
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
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
        localDbManager.insertOrUpdatePersonInLocal(rl_Person(fbPerson))
            .doOnComplete {
                uploadPersonAndDownloadAgain(fbPerson)
                .updatePersonInLocal()
                .subscribeOn(Schedulers.io())
                .subscribe()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun uploadPersonAndDownloadAgain(fbPerson: fb_Person): Single<fb_Person> =
        remoteDbManager
            .uploadPerson(fbPerson)
            .andThen(remoteDbManager.downloadPerson(fbPerson.patientId, fbPerson.projectId))

    private fun Single<out fb_Person>.updatePersonInLocal(): Completable =
        flatMapCompletable {
            localDbManager.insertOrUpdatePersonInLocal(rl_Person(it))
        }

    override fun loadPerson(destinationList: MutableList<Person>, projectId: String, guid: String, callback: DataCallback) {
        val result = localDbManager.loadPersonsFromLocal(
            projectId = projectId,
            patientId = guid).map { it.libPerson }

        if (result.isEmpty()) {
            remoteDbManager.downloadPerson(guid, projectId)
                .subscribeBy(
                    onSuccess = {
                        destinationList.add(rl_Person(it).libPerson)
                        callback.onSuccess()
                    },
                    onError = { callback.onFailure(DATA_ERROR.NOT_FOUND) })
        } else {
            destinationList.add(result.first())
            callback.onSuccess()
        }
    }

    override fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?) {
        val result = when (group) {
            Constants.GROUP.GLOBAL -> localDbManager.loadPersonsFromLocal().map { it.libPerson }
            Constants.GROUP.USER -> localDbManager.loadPersonsFromLocal(userId = userId).map { it.libPerson }
            Constants.GROUP.MODULE -> localDbManager.loadPersonsFromLocal(moduleId = moduleId).map { it.libPerson }
        }
        destinationList.addAll(result)
        callback?.onSuccess()
    }

    override fun getPeopleCount(personId: String?,
                                projectId: String?,
                                userId: String?,
                                moduleId: String?,
                                toSync: Boolean?): Int =
        localDbManager.getPersonsCountFromLocal(projectId, personId, userId, moduleId, toSync)

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
}
