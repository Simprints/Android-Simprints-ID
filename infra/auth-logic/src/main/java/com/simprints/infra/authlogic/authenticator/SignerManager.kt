package com.simprints.infra.authlogic.authenticator

import com.simprints.core.DispatcherIO
import com.simprints.infra.authlogic.securitystate.SecurityStateScheduler
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class SignerManager @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val eventSyncManager: EventSyncManager,
    private val securityStateScheduler: SecurityStateScheduler,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val simNetwork: SimNetwork,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {

    suspend fun signIn(projectId: String, userId: String, token: Token) = withContext(dispatcher) {
        authStore.signIn(token)
        authStore.storeCredentials(projectId, userId)
        configManager.refreshProject(projectId)
        securityStateScheduler.scheduleSecurityStateCheck()
    }

    suspend fun signOut() = withContext(dispatcher) {
        securityStateScheduler.cancelSecurityStateCheck()
        authStore.cleanCredentials()
        authStore.signOut()

        // Cancel all background sync
        eventSyncManager.cancelScheduledSync()
        // TODO imageUpSyncScheduler.cancelImageUpSync()
        configManager.cancelScheduledSyncConfiguration()

        eventSyncManager.deleteSyncInfo()
        simNetwork.resetApiBaseUrl()
        configManager.clearData()
        recentUserActivityManager.clearRecentActivity()

        Simber.tag(LoggingConstants.CrashReportTag.LOGOUT.name).i("Signed out")
    }

}
