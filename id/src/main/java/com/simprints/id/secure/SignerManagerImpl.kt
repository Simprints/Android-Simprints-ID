package com.simprints.id.secure

import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable

open class SignerManagerImpl(private var projectRepository: ProjectRepository,
                             private val remote: RemoteDbManager,
                             private val loginInfoManager: LoginInfoManager,
                             private val preferencesManager: PreferencesManager,
                             private val downSyncScopeRepository: PeopleDownSyncScopeRepository,
                             private val syncManager: SyncManager) : SignerManager {

    override fun signIn(projectId: String, userId: String, token: Token): Completable =
        remote.signIn(token.value)
            .andThen(storeCredentials(userId, projectId))
            .andThen(completableWithSuspend {
                projectRepository.loadAndRefreshCache(projectId)
                    ?: throw Exception("project not found")
            })
            .andThen(scheduleSyncs(projectId, userId))
            .trace("signIn")

    private fun storeCredentials(userId: String, projectId: String) =
        Completable.fromAction {
            loginInfoManager.storeCredentials(projectId, userId)
        }

    @Suppress("UNUSED_PARAMETER")
    private fun scheduleSyncs(projectId: String, userId: String): Completable =
        Completable.fromAction {
            syncManager.scheduleBackgroundSyncs()
        }

    override suspend fun signOut() {
        //TODO: move peopleUpSyncMaster to SyncScheduler and call .pause in CheckLoginPresenter.checkSignedInOrThrow
        //If you user clears the data (then doesn't call signout), workers still stay scheduled.
        loginInfoManager.cleanCredentials()
        remote.signOut()
        downSyncScopeRepository.deleteAll()
        syncManager.cancelBackgroundSyncs()
        syncManager.deleteSyncHistory()
        preferencesManager.clearAllSharedPreferencesExceptRealmKeys()
    }
}
