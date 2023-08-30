package com.simprints.infra.authlogic.authenticator

import com.simprints.core.DispatcherIO
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
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
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val simNetwork: SimNetwork,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {

    val signedInProjectId: String
        get() = authStore.signedInProjectId

    suspend fun signIn(projectId: String, token: Token) = withContext(dispatcher) {
        try {
            // Store Firebase token so it can be used by ConfigManager
            authStore.storeFirebaseToken(token)
            configManager.refreshProject(projectId)
            configManager.refreshProjectConfiguration(projectId)
            securityStateScheduler.scheduleSecurityStateCheck()
            // Only store credentials if all other calls succeeded. This avoids the undefined state
            // where credentials are store (i.e. user is considered logged in) but project configuration
            // is missing
            authStore.storeCredentials(projectId)
        } catch (e: Exception) {
            authStore.clearFirebaseToken()
            configManager.clearData()
            securityStateScheduler.cancelSecurityStateCheck()
            authStore.cleanCredentials()

            throw e
        }
    }

    suspend fun signOut() = withContext(dispatcher) {
        securityStateScheduler.cancelSecurityStateCheck()
        authStore.cleanCredentials()
        authStore.clearFirebaseToken()

        // Cancel all background sync
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        configManager.cancelScheduledSyncConfiguration()

        eventSyncManager.deleteSyncInfo()
        simNetwork.resetApiBaseUrl()
        configManager.clearData()
        recentUserActivityManager.clearRecentActivity()

        Simber.tag(LoggingConstants.CrashReportTag.LOGOUT.name).i("Signed out")
    }

}
