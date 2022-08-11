package com.simprints.id.secure

import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.network.SimNetwork

open class SignerManagerImpl(
    private var projectRepository: ProjectRepository,
    private val loginManager: LoginManager,
    private val preferencesManager: PreferencesManager,
    private val eventSyncManager: EventSyncManager,
    private val syncManager: SyncManager,
    private val securityStateScheduler: SecurityStateScheduler,
    private val longConsentRepository: LongConsentRepository,
    private val baseUrlProvider: SimNetwork,
    private val remoteConfigWrapper: RemoteConfigWrapper
) : SignerManager {

    override suspend fun signIn(projectId: String, userId: String, token: Token) {
        loginManager.signIn(token)
        loginManager.storeCredentials(projectId, userId)
        projectRepository.loadFromRemoteAndRefreshCache(projectId)
            ?: throw Exception("project not found")
        securityStateScheduler.scheduleSecurityStateCheck()
    }

    override suspend fun signOut() {
        //TODO: move peopleUpSyncMaster to SyncScheduler and call .pause in CheckLoginPresenter.checkSignedInOrThrow
        //If you user clears the data (then doesn't call signout), workers still stay scheduled.
        securityStateScheduler.cancelSecurityStateCheck()
        loginManager.cleanCredentials()
        loginManager.signOut()
        syncManager.cancelBackgroundSyncs()
        eventSyncManager.deleteSyncInfo()
        preferencesManager.clearAllSharedPreferences()
        longConsentRepository.deleteLongConsents()
        baseUrlProvider.resetApiBaseUrl()
        remoteConfigWrapper.clearRemoteConfig()
    }

}
