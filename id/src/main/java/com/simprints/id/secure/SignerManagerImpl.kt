package com.simprints.id.secure

import com.simprints.core.DispatcherIO
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.sync.SyncManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO move into its own module
class SignerManagerImpl @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val eventSyncManager: EventSyncManager,
    private val syncManager: SyncManager,
    private val securityStateScheduler: SecurityStateScheduler,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val simNetwork: SimNetwork,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : SignerManager, com.simprints.feature.dashboard.settings.about.SignerManager {

    override suspend fun signIn(projectId: String, userId: String, token: Token) = withContext(dispatcher) {
        authStore.signIn(token)
        authStore.storeCredentials(projectId, userId)
        configManager.refreshProject(projectId)
        securityStateScheduler.scheduleSecurityStateCheck()
    }

    override suspend fun signOut() = withContext(dispatcher) {
        //TODO: move peopleUpSyncMaster to SyncScheduler and call .pause in CheckLoginPresenter.checkSignedInOrThrow
        //If you user clears the data (then doesn't call signout), workers still stay scheduled.
        securityStateScheduler.cancelSecurityStateCheck()
        authStore.cleanCredentials()
        authStore.signOut()
        syncManager.cancelBackgroundSyncs()
        eventSyncManager.deleteSyncInfo()
        simNetwork.resetApiBaseUrl()
        configManager.clearData()
        recentUserActivityManager.clearRecentActivity()

        Simber.tag(LoggingConstants.CrashReportTag.LOGOUT.name).i("Signed out")
    }

}
