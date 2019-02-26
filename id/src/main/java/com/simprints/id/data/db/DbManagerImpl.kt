package com.simprints.id.data.db

import com.simprints.id.data.analytics.eventData.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventData.models.domain.events.EnrollmentEvent
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrollmentEvent
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toDomainPerson
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.IdPerson
import com.simprints.id.domain.Project
import com.simprints.id.domain.matching.IdentificationResult
import com.simprints.id.domain.matching.VerificationResult
import com.simprints.id.domain.refusal_form.IdRefusalForm
import com.simprints.id.domain.toLibPerson
import com.simprints.id.secure.models.Tokens
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.session.Session
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import com.simprints.id.domain.fingerprint.Person as LibPerson

open class DbManagerImpl(override val local: LocalDbManager,
                         override val remote: RemoteDbManager,
                         private val secureDataManager: SecureDataManager,
                         private val loginInfoManager: LoginInfoManager,
                         private val preferencesManager: PreferencesManager,
                         private val sessionEventsManager: SessionEventsManager,
                         override val remotePeopleManager: RemotePeopleManager,
                         override val remoteProjectManager: RemoteProjectManager,
                         private val timeHelper: TimeHelper,
                         private val peopleUpSyncMaster: PeopleUpSyncMaster,
                         private val syncStatusDatabase: SyncStatusDatabase) : DbManager {

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
        //TODO: move peopleUpSyncMaster to SyncScheduler and call .pause in CheckLoginPresenter.checkSignedInOrThrow
        //If you user clears the data (then doesn't call signout), workers still stay scheduled.
        peopleUpSyncMaster.pause(loginInfoManager.signedInProjectId/*, loginInfoManager.signedInUserId*/) // TODO: uncomment userId when multitenancy is properly implemented
        loginInfoManager.cleanCredentials()
        remote.signOutOfRemoteDb()
        syncStatusDatabase.downSyncDao.deleteAll()
        syncStatusDatabase.upSyncDao.deleteAll()
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
                    .updateSession {
                        it.events.add(EnrollmentEvent(
                            it.nowRelativeToStartTime(timeHelper),
                            fbPerson.patientId
                        ))
                    }
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
                remotePeopleManager.downloadPerson(guid, projectId).subscribeBy(
                    onSuccess = { fbPerson ->
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
                remotePeopleManager
                    .downloadPerson(guid, loginInfoManager.getSignedInProjectIdOrEmpty())
                    .map { fbPerson ->
                        PersonFetchResult(fbPerson.toDomainPerson(), true)
                    }
            }


    override fun loadPeople(destinationList: MutableList<LibPerson>, group: GROUP, callback: DataCallback?) {
        val people = when (group) {
            GROUP.GLOBAL -> local.loadPeopleFromLocal()
            GROUP.USER -> local.loadPeopleFromLocal(userId = loginInfoManager.getSignedInUserIdOrEmpty())
            GROUP.MODULE -> local.loadPeopleFromLocal(moduleId = preferencesManager.moduleId)
        }
            .blockingGet()
            .map(IdPerson::toLibPerson)
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
        remoteProjectManager.loadProjectFromRemote(projectId).flatMap {
            local.saveProjectIntoLocal(it)
                .andThen(Single.just(it))
        }.trace("refreshProjectInfoWithServer")

    override fun calculateNPatientsToDownSync(projectId: String, userId: String?, moduleId: String?): Single<Int> =
        remotePeopleManager.getNumberOfPatients(projectId, userId, moduleId).flatMap { nPatientsOnServer ->
            local.getPeopleCountFromLocal(userId = userId, moduleId = moduleId, toSync = false).map {
                Math.max(nPatientsOnServer - it, 0)
            }
        }

    override fun getPeopleCountFromLocalForSyncScope(syncScope: SyncScope): Single<Int> =
        Single.just(
            syncScope.toSubSyncScopes().map {
                local.getPeopleCountFromLocal(
                    userId = it.userId,
                    moduleId = it.moduleId).blockingGet()
            }.sum()
        )

    override fun saveIdentification(probe: LibPerson, matchSize: Int, matches: List<IdentificationResult>) {
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

    override fun saveVerification(probe: LibPerson, match: VerificationResult?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT) {
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

    override fun saveRefusalForm(refusalForm: IdRefusalForm) {
        remote.saveRefusalFormInRemote(
            refusalForm,
            loginInfoManager.getSignedInProjectIdOrEmpty(),
            loginInfoManager.getSignedInUserIdOrEmpty(),
            preferencesManager.sessionId)
    }

    override fun saveSession(session: Session) {
        remote.saveSessionInRemote(session)
    }
}
