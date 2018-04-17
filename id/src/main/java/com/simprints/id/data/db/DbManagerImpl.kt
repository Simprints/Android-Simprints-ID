package com.simprints.id.data.db

import com.simprints.id.data.db.dbRecovery.LocalDbRecovererImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.models.LocalDbKey
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.models.Project
import com.simprints.id.data.db.remote.FirebaseManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.sync.SyncExecutor
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


class DbManagerImpl(override val localDbManager: LocalDbManager,
                    override val remoteDbManager: RemoteDbManager) :
    DbManager,
    RemoteDbManager by remoteDbManager {

    override fun initialiseDb() {
        remoteDbManager.initialiseRemoteDb()
    }

    override fun getLocalKeyAndSignInToLocal(projectId: String): Completable =
        remoteDbManager.getLocalDbKeyFromRemote(projectId).signInToLocal()

    override fun signIn(projectId: String, tokens: Tokens): Completable =
        remoteDbManager.signInToRemoteDb(tokens).andThen(getLocalKeyAndSignInToLocal(projectId))

    private fun Single<out LocalDbKey>.signInToLocal(): Completable =
        flatMapCompletable {
            localDbManager.signInToLocal()
        }

    override fun signOut() {
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
                    .subscribeBy (onComplete = {}, onError = { it.printStackTrace() })
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun uploadPersonAndDownloadAgain(fbPerson: fb_Person): Single<fb_Person> =
        remoteDbManager
            .uploadPerson(fbPerson)
            .andThen(remoteDbManager.downloadPerson(fbPerson.patientId, fbPerson.projectId))

    private fun Single<out fb_Person>.updatePersonInLocal(): Completable = flatMapCompletable {
        localDbManager.insertOrUpdatePersonInLocal(rl_Person(it))
    }

    override fun loadPerson(destinationList: MutableList<Person>,
                            projectId: String,
                            guid: String,
                            callback: DataCallback) {

        localDbManager.loadPersonFromLocal(guid).subscribe({
            destinationList.add(it)
            callback.onSuccess()
        }, {
            remoteDbManager.downloadPerson(guid, projectId).subscribeBy(
                onSuccess = { destinationList.add(rl_Person(it).libPerson); callback.onSuccess() },
                onError = { callback.onFailure(DATA_ERROR.NOT_FOUND) })
        })
    }

    override fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, userId: String, moduleId: String, callback: DataCallback?) {
        val result = when (group) {
            Constants.GROUP.GLOBAL -> localDbManager.loadPeopleFromLocal().blockingGet().map { it.libPerson }
            Constants.GROUP.USER -> localDbManager.loadPeopleFromLocal(userId = userId).blockingGet().map { it.libPerson }
            Constants.GROUP.MODULE -> localDbManager.loadPeopleFromLocal(moduleId = moduleId).blockingGet().map { it.libPerson }
        }
        destinationList.addAll(result)
        callback?.onSuccess()
    }

    override fun loadProject(projectId: String): Single<Project> =
        localDbManager.loadProjectFromLocal(projectId)
            .doAfterSuccess {
                refreshProjectInfoWithServer(projectId)
            }
            .onErrorResumeNext {
                refreshProjectInfoWithServer(projectId)
            }

    override fun refreshProjectInfoWithServer(projectId: String): Single<Project> =
        remoteDbManager.loadProjectFromRemote(projectId).doAfterSuccess {
            localDbManager.saveProjectIntoLocal(it)
        }

    override fun calculateNPatientsToDownSync(nPatientsOnServerForSyncParam: Int, syncParams: SyncTaskParameters): Single<Int> =
        localDbManager.getPeopleCountFromLocal(
            userId = syncParams.userId,
            moduleId = syncParams.moduleId,
            toSync = false).map {
                Math.max(nPatientsOnServerForSyncParam - it, 0)
            }

    override fun getPeopleCount(personId: String?,
                                projectId: String?,
                                userId: String?,
                                moduleId: String?,
                                toSync: Boolean?): Single<Int> =
        localDbManager.getPeopleCountFromLocal(
            patientId = personId,
            userId = userId,
            moduleId = moduleId,
            toSync = toSync
        )

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
        SyncExecutor(
            this,
            JsonHelper.gson
        ).sync(interrupted, parameters)

    override fun recoverLocalDb(projectId: String, userId: String, androidId: String, moduleId: String, group: Constants.GROUP): Completable {
        val firebaseManager = remoteDbManager as FirebaseManager
        val realmManager = localDbManager as RealmDbManagerImpl
        return LocalDbRecovererImpl(realmManager, firebaseManager, projectId, userId, androidId, moduleId, group).recoverDb()
    }

}
