package com.simprints.id.secure

import com.simprints.core.tools.extentions.completableWithSuspend
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.syncscope.DownSyncScopeRepository
import com.simprints.id.data.db.syncstatus.upsyncinfo.UpSyncDao
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster
import com.simprints.id.tools.extensions.trace
import io.reactivex.Completable

open class SignerManagerImpl(private var projectRepository: ProjectRepository,
                             private val remote: RemoteDbManager,
                             private val loginInfoManager: LoginInfoManager,
                             private val preferencesManager: PreferencesManager,
                             private val downSyncScopeRepository: DownSyncScopeRepository,
                             private val peopleUpSyncMaster: PeopleUpSyncMaster,
                             private val upSyncDao: UpSyncDao) : SignerManager {

    override fun signIn(projectId: String, userId: String, token: Token): Completable =
        remote.signIn(token.value)
            .andThen(storeCredentials(userId, projectId))
            .andThen(completableWithSuspend {
                projectRepository.loadAndRefreshCache(projectId)
                    ?: throw Exception("project not found")
            })
            .andThen(resumePeopleUpSync(projectId, userId))
            .trace("signIn")

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
        remote.signOut()
        downSyncScopeRepository.deleteAll()
        upSyncDao.deleteAll()
        preferencesManager.clearAllSharedPreferencesExceptRealmKeys()
    }
}
