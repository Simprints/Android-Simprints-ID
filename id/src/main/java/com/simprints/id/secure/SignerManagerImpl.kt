package com.simprints.id.secure

import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.secure.models.Token
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.securitystate.SecurityStateScheduler

open class SignerManagerImpl(
    private var projectRepository: ProjectRepository,
    private val remote: RemoteDbManager,
    private val loginInfoManager: LoginInfoManager,
    private val preferencesManager: PreferencesManager,
    private val eventSyncManager: EventSyncManager,
    private val syncManager: SyncManager,
    private val securityStateScheduler: SecurityStateScheduler,
    private val longConsentRepository: LongConsentRepository,
    private val eventRepository: com.simprints.eventsystem.event.EventRepository,
    private val baseUrlProvider: BaseUrlProvider,
    private val remoteConfigWrapper: RemoteConfigWrapper
) : SignerManager {

    override suspend fun signIn(projectId: String, userId: String, token: Token) {
        remote.signIn(token.value)
        loginInfoManager.storeCredentials(projectId, userId)
        projectRepository.loadFromRemoteAndRefreshCache(projectId)
            ?: throw Exception("project not found")
        securityStateScheduler.scheduleSecurityStateCheck()
    }

    override suspend fun signOut() {
        //TODO: move peopleUpSyncMaster to SyncScheduler and call .pause in CheckLoginPresenter.checkSignedInOrThrow
        //If you user clears the data (then doesn't call signout), workers still stay scheduled.
        securityStateScheduler.cancelSecurityStateCheck()
        loginInfoManager.cleanCredentials()
        remote.signOut()
        syncManager.cancelBackgroundSyncs()
        eventSyncManager.deleteSyncInfo()
        preferencesManager.clearAllSharedPreferencesExceptRealmKeys()
        longConsentRepository.deleteLongConsents()
        baseUrlProvider.resetApiBaseUrl()
        remoteConfigWrapper.clearRemoteConfig()
    }

}
