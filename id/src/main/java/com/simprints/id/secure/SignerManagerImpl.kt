package com.simprints.id.secure

import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager

open class SignerManagerImpl(
    private var projectRepository: ProjectRepository,
    private val remote: RemoteDbManager,
    private val loginInfoManager: LoginInfoManager,
    private val preferencesManager: PreferencesManager,
    private val peopleSyncManager: PeopleSyncManager,
    private val syncManager: SyncManager
) : SignerManager {

    override suspend fun signIn(projectId: String, userId: String, token: Token) {
        remote.signIn(token.value)
        storeCredentials(userId, projectId)
        projectRepository.loadFromRemoteAndRefreshCache(projectId)
            ?: throw Exception("project not found")
    }

    private fun storeCredentials(userId: String, projectId: String) =
        loginInfoManager.storeCredentials(projectId, userId)

    override suspend fun signOut() {
        //TODO: move peopleUpSyncMaster to SyncScheduler and call .pause in CheckLoginPresenter.checkSignedInOrThrow
        //If you user clears the data (then doesn't call signout), workers still stay scheduled.
        loginInfoManager.cleanCredentials()
        remote.signOut()
        syncManager.cancelBackgroundSyncs()
        peopleSyncManager.deleteSyncInfo()
        preferencesManager.clearAllSharedPreferencesExceptRealmKeys()
    }

}
