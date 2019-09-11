package com.simprints.id.data.db

import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.syncstatus.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable
import kotlinx.coroutines.runBlocking

open class DbManagerImpl(private var projectRepository: ProjectRepository,
                         private val remote: RemoteDbManager,
                         private val loginInfoManager: LoginInfoManager,
                         private val preferencesManager: PreferencesManager,
                         private val peopleUpSyncMaster: PeopleUpSyncMaster,
                         private val syncStatusDatabase: SyncStatusDatabase) : DbManager {

    override fun signIn(projectId: String, userId: String, token: Token): Completable =
        remote.signInToRemoteDb(token.value)
            .andThen(storeCredentials(userId, projectId))
            .andThen(Completable.fromAction { runBlocking { projectRepository.loadAndRefreshCache(projectId) } })
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

    //TODO: Move this logic to the caller = Orchestrator
//    override fun savePerson(person: Person): Completable =
//        Completable.create {
//            runBlocking {
//                try {
//                    personLocalDataSource.insertOrUpdate(listOf(person.apply { toSync = true }))
//                    it.onComplete()
//                } catch (t: Throwable) {
//                    t.printStackTrace()
//                    it.onError(t)
//                }
//            }
//        }.doOnComplete {
//            sessionEventsManager
//                .updateSession {
//                    it.addEvent(EnrolmentEvent(
//                        timeHelper.now(),
//                        person.patientId
//                    ))
//                }
//                .andThen(scheduleUpsync(person.projectId, person.userId))
//                .subscribeOn(Schedulers.io())
//                .subscribeBy(onComplete = {}, onError = {
//                    it.printStackTrace()
//                })
//        }
//    @Suppress("UNUSED_PARAMETER")
//    private fun scheduleUpsync(projectId: String, userId: String): Completable = Completable.fromAction {
//        peopleUpSyncMaster.schedule(projectId/*, userId*/) // TODO: uncomment userId when multitenancy is properly implemented
//    }
}
