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
import com.simprints.id.data.db.remote.models.toDomainPerson
import com.simprints.id.data.db.sync.SyncExecutor
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Person
import com.simprints.id.domain.Project
import com.simprints.id.domain.toLibPerson
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.session.Session
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.trace
import com.simprints.id.tools.json.JsonHelper
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
import com.simprints.libcommon.Person as LibPerson

open class DbManagerImpl(override val local: LocalDbManager,
                         override val remote: RemoteDbManager,
                         private val secureDataManager: SecureDataManager,
                         private val loginInfoManager: LoginInfoManager,
                         private val preferencesManager: PreferencesManager,
                         private val sessionEventsManager: SessionEventsManager,
                         private val timeHelper: TimeHelper,
                         private val peopleUpSyncMaster: PeopleUpSyncMaster) : DbManager {

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

    override fun signIn(projectId: String, userId: String, tokens: Tokens): Completable =
        remote.signInToRemoteDb(tokens)
            .andThen(signInToLocal(projectId))
            .andThen(refreshProjectInfoWithServer(projectId))
            .flatMapCompletable(storeCredentials(userId))
            .andThen(resumePeopleUpSync(projectId, userId))
            .trace("signInToRemoteDb")

    private fun signInToLocal(projectId: String): Completable =
        Completable.create {
            try {
                local.signInToLocal(secureDataManager.getLocalDbKeyOrThrow(projectId))
                it.onComplete()
            } catch (t: Throwable) {
                it.onError(t)
            }
        }

    private fun storeCredentials(userId: String) = { project: Project ->
        Completable.create {
            try {
                loginInfoManager.storeCredentials(project.id, project.legacyId, userId)
                it.onComplete()
            } catch (t: Throwable) {
                it.onError(t)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun resumePeopleUpSync(projectId: String, userId: String): Completable =
        Completable.create {
            peopleUpSyncMaster.resume(projectId/*, userId*/) // TODO: uncomment userId when multitenancy is properly implemented

            it.onComplete()
        }

    override fun signOut() {
        peopleUpSyncMaster.pause(loginInfoManager.signedInProjectId/*, loginInfoManager.signedInUserId*/) // TODO: uncomment userId when multitenancy is properly implemented
        loginInfoManager.cleanCredentials()
        remote.signOutOfRemoteDb()
        preferencesManager.clearAllSharedPreferencesExceptRealmKeys()
    }

    override fun isDbInitialised(): Boolean =
        remote.isRemoteDbInitialized()

    // Data transfer
    override fun savePerson(person: LibPerson): Completable =
        savePerson(fb_Person(
            person,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.moduleId))

    override fun savePerson(fbPerson: fb_Person): Completable = // TODO Investigate this interesting nested subscription
        local.insertOrUpdatePersonInLocal(rl_Person(fbPerson, toSync = true))
            .doOnComplete {
                sessionEventsManager
                    .updateSession({
                        it.events.add(EnrollmentEvent(
                            it.nowRelativeToStartTime(timeHelper),
                            fbPerson.patientId
                        ))
                    })
                    .andThen(scheduleUpsync(fbPerson.projectId, fbPerson.userId))
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onComplete = {}, onError = {
                        it.printStackTrace()
                    })
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .trace("savePerson")

    @Suppress("UNUSED_PARAMETER")
    private fun scheduleUpsync(projectId: String, userId: String): Completable = Completable.create {
        peopleUpSyncMaster.schedule(projectId/*, userId*/) // TODO: uncomment userId when multitenancy is properly implemented
        it.onComplete()
    }

    override fun loadPerson(destinationList: MutableList<LibPerson>,
                            projectId: String,
                            guid: String,
                            callback: DataCallback) {

        local.loadPersonFromLocal(guid).subscribeBy(
            onSuccess = { person ->
                destinationList.add(person.toLibPerson())
                callback.onSuccess(false)
            },
            onError = {
                remote.downloadPerson(guid, projectId).subscribeBy(
                    onSuccess = {  fbPerson ->
                        destinationList.add(fbPerson.toDomainPerson().toLibPerson())
                        callback.onSuccess(true)
                    },
                    onError = {
                        callback.onFailure(DATA_ERROR.NOT_FOUND)
                    }
                )
            })
    }

    override fun loadPerson(projectId: String,
                            guid: String): Single<PersonFetchResult> =
        local.loadPersonFromLocal(guid).map { PersonFetchResult(it, false) }
            .onErrorResumeNext {
                remote
                    .downloadPerson(guid, loginInfoManager.getSignedInProjectIdOrEmpty())
                    .map { fbPerson ->
                        PersonFetchResult(fbPerson.toDomainPerson(), true)
                    }
            }


    override fun loadPeople(destinationList: MutableList<LibPerson>, group: Constants.GROUP, callback: DataCallback?) {
        val people = when (group) {
            Constants.GROUP.GLOBAL -> local.loadPeopleFromLocal()
            Constants.GROUP.USER -> local.loadPeopleFromLocal(userId = loginInfoManager.getSignedInUserIdOrEmpty())
            Constants.GROUP.MODULE -> local.loadPeopleFromLocal(moduleId = preferencesManager.moduleId)
        }
            .blockingGet()
            .map(Person::toLibPerson)
        destinationList.addAll(people)
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
        remote.loadProjectFromRemote(projectId).flatMap {
            local.saveProjectIntoLocal(it)
                .andThen(Single.just(it))
        }.trace("refreshProjectInfoWithServer")

    override fun calculateNPatientsToDownSync(nPatientsOnServerForSyncParam: Int, syncParams: SyncTaskParameters): Single<Int> =
        syncParams.moduleIds?.let { moduleIds ->
            sumCountsForEachModule(moduleIds).map {
                Math.max(nPatientsOnServerForSyncParam - it, 0)
            }
        } ?: local.getPeopleCountFromLocal(
            userId = syncParams.userId,
            moduleId = null,
            toSync = false).map {
            Math.max(nPatientsOnServerForSyncParam - it, 0)
        }

    private fun sumCountsForEachModule(moduleIds: Set<String>): Single<Int> =
        Single.merge(moduleIds.map {
            local.getPeopleCountFromLocal(moduleId =  it, toSync = false)
        }).toList().map { it.sum() }

    override fun getPeopleCount(group: Constants.GROUP): Single<Int> =
        when (group) {
            Constants.GROUP.GLOBAL ->
                local.getPeopleCountFromLocal()
            Constants.GROUP.USER ->
                local.getPeopleCountFromLocal(userId = loginInfoManager.getSignedInUserIdOrEmpty())
            Constants.GROUP.MODULE ->
                sumCountsForEachModule(preferencesManager.selectedModules)
        }

    override fun saveIdentification(probe: LibPerson, matchSize: Int, matches: List<Identification>) {
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

    override fun saveVerification(probe: LibPerson, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
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
