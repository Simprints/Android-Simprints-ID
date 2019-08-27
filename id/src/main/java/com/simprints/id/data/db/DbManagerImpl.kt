package com.simprints.id.data.db

import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrolmentEvent
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.PeopleCount
import com.simprints.id.domain.Person
import com.simprints.id.domain.Project
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

open class DbManagerImpl(override val local: LocalDbManager,
                         override val remote: RemoteDbManager,
                         private val loginInfoManager: LoginInfoManager,
                         private val preferencesManager: PreferencesManager,
                         private val sessionEventsManager: SessionEventsManager,
                         override val remotePeopleManager: RemotePeopleManager,
                         override val remoteProjectManager: RemoteProjectManager,
                         private val timeHelper: TimeHelper,
                         private val peopleUpSyncMaster: PeopleUpSyncMaster,
                         private val syncStatusDatabase: SyncStatusDatabase) : DbManager {

    override fun signIn(projectId: String, userId: String, token: Token): Completable =
        remote.signInToRemoteDb(token.value)
            .andThen(storeCredentials(userId, projectId))
            .andThen(refreshProjectInfoWithServer(projectId).ignoreElement())
            .andThen(resumePeopleUpSync(projectId, userId))
            .trace("signInToRemoteDb")

    private fun storeCredentials(userId: String, projectId: String) =
        Completable.fromAction {
            loginInfoManager.storeCredentials(projectId, userId)
        }

    @Suppress("UNUSED_PARAMETER")
    private fun resumePeopleUpSync(projectId: String, userId: String): Completable =
        Completable.fromAction {
            peopleUpSyncMaster.resume(projectId/*, userId*/) // TODO: uncomment userId when multitenancy is properly implemented
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

    override fun savePerson(person: Person): Completable =
        local.insertOrUpdatePersonInLocal(person.apply { toSync = true })
            .doOnComplete {
                sessionEventsManager
                    .updateSession {
                        it.addEvent(EnrolmentEvent(
                            timeHelper.now(),
                            person.patientId
                        ))
                    }
                    .andThen(scheduleUpsync(person.projectId, person.userId))
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(onComplete = {}, onError = {
                        it.printStackTrace()
                    })
            }

    @Suppress("UNUSED_PARAMETER")
    private fun scheduleUpsync(projectId: String, userId: String): Completable = Completable.fromAction {
        peopleUpSyncMaster.schedule(projectId/*, userId*/) // TODO: uncomment userId when multitenancy is properly implemented
    }

    override fun loadPerson(projectId:String, guid: String): Single<PersonFetchResult> =
        local.loadPersonFromLocal(guid).map { PersonFetchResult(it, false) }
            .onErrorResumeNext {
                remotePeopleManager
                    .downloadPerson(guid, projectId)
                    .map { person ->
                        PersonFetchResult(person, true)
                    }
            }

    override fun loadPeople(projectId: String, userId: String?, moduleId: String?): Single<List<Person>> =
        local.loadPeopleFromLocal(projectId, userId = userId, moduleId = moduleId)

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

    override fun getPeopleCountToDownSync(syncScope: SyncScope): Single<List<PeopleCount>> =
        remotePeopleManager.getDownSyncPeopleCount(syncScope).flatMap { peopleCountInRemote ->
            getPeopleCountFromLocalForSyncScope(syncScope).map { peopleCountsInLocal ->
               calculateDifferenceBetweenRemoteAndLocal(peopleCountInRemote, peopleCountsInLocal)
            }
        }

    private fun calculateDifferenceBetweenRemoteAndLocal(peopleCountInRemote: List<PeopleCount>,
                                                         peopleCountsInLocal: List<PeopleCount>): List<PeopleCount> =
        peopleCountInRemote.map { remotePeopleCount ->
            val localCount = peopleCountsInLocal.find {
                it.projectId == remotePeopleCount.projectId &&
                it.userId == remotePeopleCount.userId &&
                it.moduleId == remotePeopleCount.moduleId &&
                it.modes?.joinToString() == remotePeopleCount.modes?.joinToString()
            }?.count ?: 0

            remotePeopleCount.copy(count = remotePeopleCount.count - localCount)
        }
    
    override fun getPeopleCountFromLocalForSyncScope(syncScope: SyncScope): Single<List<PeopleCount>> =
        Single.just(
            syncScope.toSubSyncScopes().map {
                PeopleCount(it.projectId,
                    it.userId,
                    it.moduleId,
                    syncScope.modes,
                    local.getPeopleCountFromLocal(
                        projectId = it.projectId,
                        userId = it.userId,
                        moduleId = it.moduleId).blockingGet())
            }
        )
}
