package com.simprints.id.data.db

import com.simprints.id.data.analytics.eventData.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.events.EnrollmentEvent
import com.simprints.id.data.db.dbRecovery.LocalDbRecovererImpl
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.RealmDbManagerImpl
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.remote.FirebaseManagerImpl
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.db.remote.sessions.RemoteSessionsManager
import com.simprints.id.data.db.sync.SyncExecutor
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Project
import com.simprints.id.exceptions.safe.setup.FetchingGuidForVerificationFailedException
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.session.Session
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.trace
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
import java.util.*

open class DbManagerImpl(override val local: LocalDbManager,
                    override val remote: RemoteDbManager,
                    private val secureDataManager: SecureDataManager,
                    private val loginInfoManager: LoginInfoManager,
                    private val preferencesManager: PreferencesManager,
                    private val sessionEventsManager: SessionEventsManager,
                    override val remotePeopleManager: RemotePeopleManager,
                    override val remoteProjectManager: RemoteProjectManager,
                    private val timeHelper: TimeHelper) : DbManager {

    override fun initialiseDb() {
        remote.initialiseRemoteDb()
        val projectId = loginInfoManager.getSignedInProjectIdOrEmpty()
        if (projectId.isNotEmpty()) {
            try {
                local.signInToLocal(secureDataManager.getLocalDbKeyOrThrow(projectId))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun signIn(projectId: String, tokens: Tokens): Completable =
        remote.signInToRemoteDb(tokens)
            .andThen {
                try {
                    local.signInToLocal(secureDataManager.getLocalDbKeyOrThrow(projectId))
                    it.onComplete()
                } catch (t: Throwable) {
                    it.onError(t)
                }
            }
            .trace("signInToRemoteDb")

    override fun signOut() {
        remote.signOutOfRemoteDb()
    }

    override fun isDbInitialised(): Boolean =
        remote.isRemoteDbInitialized()

    // Data transfer
    override fun savePerson(person: Person): Completable =
        savePerson(fb_Person(
            person,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.moduleId))

    override fun savePerson(fbPerson: fb_Person): Completable = // TODO Investigate this interesting nested subscription
        local.insertOrUpdatePersonInLocal(rl_Person(fbPerson))
            .doOnComplete {
                sessionEventsManager.updateSession({
                    it.events.add(EnrollmentEvent(
                        it.nowRelativeToStartTime(timeHelper),
                        fbPerson.patientId
                    ))
                }).andThen(uploadPersonAndDownloadAgain(fbPerson))
                    .updatePersonInLocal()
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onComplete = {}, onError = {
                        it.printStackTrace()
                    })
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .trace("savePerson")

    private fun uploadPersonAndDownloadAgain(fbPerson: fb_Person): Single<fb_Person> =
        remotePeopleManager
            .uploadPerson(fbPerson)
            .andThen(remotePeopleManager.downloadPerson(fbPerson.patientId, fbPerson.projectId))
            .trace("uploadPersonAndDownloadAgain")

    private fun Single<out fb_Person>.updatePersonInLocal(): Completable = flatMapCompletable {
        local.insertOrUpdatePersonInLocal(rl_Person(it))
    }.trace("updatePersonInLocal")

    override fun loadPerson(destinationList: MutableList<Person>,
                            projectId: String,
                            guid: String,
                            callback: DataCallback) {

        local.loadPersonFromLocal(guid).subscribeBy(onSuccess = {
            destinationList.add(it)
            callback.onSuccess(false)
        }, onError = { e ->
            remotePeopleManager.downloadPerson(guid, projectId).subscribeBy(
                onSuccess = { destinationList.add(rl_Person(it).libPerson); callback.onSuccess(true) },
                onError = { callback.onFailure(DATA_ERROR.NOT_FOUND) })
        })
    }

    override fun loadPerson(projectId: String,
                            guid: String): Single<PersonFetchResult> =
        local.loadPersonFromLocal(guid).map { PersonFetchResult(it, false) }
            .onErrorResumeNext {
                remotePeopleManager.downloadPerson(guid, loginInfoManager.getSignedInProjectIdOrEmpty())
                    .map { PersonFetchResult(rl_Person(it).libPerson, true) }
            }


    override fun loadPeople(destinationList: MutableList<Person>, group: Constants.GROUP, callback: DataCallback?) {
        val result = when (group) {
            Constants.GROUP.GLOBAL -> local.loadPeopleFromLocal().blockingGet().map { it.libPerson }
            Constants.GROUP.USER -> local.loadPeopleFromLocal(userId = loginInfoManager.getSignedInUserIdOrEmpty()).blockingGet().map { it.libPerson }
            Constants.GROUP.MODULE -> local.loadPeopleFromLocal(moduleId = preferencesManager.moduleId).blockingGet().map { it.libPerson }
        }
        destinationList.addAll(result)
        callback?.onSuccess(false)
    }

    override fun loadProject(projectId: String): Single<Project> =
        local.loadProjectFromLocal(projectId)
            .doAfterSuccess {
                refreshProjectInfoWithServer(projectId)
            }
            .onErrorResumeNext {
                refreshProjectInfoWithServer(projectId)
            }

    override fun refreshProjectInfoWithServer(projectId: String): Single<Project> =
        remoteProjectManager.loadProjectFromRemote(projectId).flatMap {
            local.saveProjectIntoLocal(it)
                .andThen(Single.just(it))
        }.trace("refreshProjectInfoWithServer")

    override fun calculateNPatientsToDownSync(nPatientsOnServerForSyncParam: Int, syncParams: SyncTaskParameters): Single<Int> =
        local.getPeopleCountFromLocal(
            userId = syncParams.userId,
            moduleId = syncParams.moduleId,
            toSync = false).map {
            Math.max(nPatientsOnServerForSyncParam - it, 0)
        }

    override fun getPeopleCount(group: Constants.GROUP): Single<Int> =
        when (group) {
            Constants.GROUP.GLOBAL ->
                local.getPeopleCountFromLocal()
            Constants.GROUP.USER ->
                local.getPeopleCountFromLocal(userId = loginInfoManager.getSignedInUserIdOrEmpty())
            Constants.GROUP.MODULE ->
                local.getPeopleCountFromLocal(userId = loginInfoManager.getSignedInUserIdOrEmpty(), moduleId = preferencesManager.moduleId)
        }

    override fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>) {
        preferencesManager.lastIdentificationDate = Date()
        remote.saveIdentificationInRemote(
            probe,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.moduleId,
            preferencesManager.deviceId,
            matchSize,
            matches,
            preferencesManager.sessionId)
    }

    override fun updateIdentification(projectId: String, selectedGuid: String, sessionId: String) {
        remote.updateIdentificationInRemote(
            projectId,
            selectedGuid,
            preferencesManager.deviceId,
            sessionId)
    }

    override fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
        preferencesManager.lastVerificationDate = Date()
        remote.saveVerificationInRemote(
            probe,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.moduleId,
            preferencesManager.deviceId,
            preferencesManager.patientId,
            match,
            preferencesManager.sessionId,
            guidExistsResult)
    }

    override fun saveRefusalForm(refusalForm: RefusalForm) {
        remote.saveRefusalFormInRemote(
            refusalForm,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.sessionId)
    }

    override fun saveSession(session: Session) {
        remote.saveSessionInRemote(session)
    }

    override fun sync(parameters: SyncTaskParameters, interrupted: () -> Boolean): Observable<Progress> =
        SyncExecutor(
            this,
            JsonHelper.gson
        ).sync(interrupted, parameters).trace("sync")

    override fun recoverLocalDb(group: Constants.GROUP): Completable {
        val firebaseManager = remote as FirebaseManagerImpl
        val realmManager = local as RealmDbManagerImpl
        return LocalDbRecovererImpl(
            realmManager,
            firebaseManager,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.deviceId,
            preferencesManager.moduleId,
            group)
            .recoverDb()
            .trace("recoverLocalDb")
    }
}
