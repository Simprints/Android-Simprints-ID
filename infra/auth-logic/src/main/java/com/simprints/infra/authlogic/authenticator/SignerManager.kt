package com.simprints.infra.authlogic.authenticator

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class SignerManager @Inject constructor(
    private val configScheduler: ProjectConfigurationScheduler,
    private val configRepository: ConfigRepository,
    private val authStore: AuthStore,
    private val eventSyncManager: EventSyncManager,
    private val securityStateScheduler: SecurityStateScheduler,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val simNetwork: SimNetwork,
    private val imageRepository: ImageRepository,
    private val eventRepository: EventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val scannerManager: ScannerManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {

    val signedInProjectId: String
        get() = authStore.signedInProjectId

    suspend fun signIn(projectId: String, token: Token) = withContext(dispatcher) {
        try {
            // Store Firebase token so it can be used by ConfigManager
            authStore.storeFirebaseToken(token)
            configRepository.refreshProject(projectId)
            // Only store credentials if all other calls succeeded. This avoids the undefined state
            // where credentials are store (i.e. user is considered logged in) but project configuration
            // is missing
            authStore.storeCredentials(projectId)
        } catch (e: Exception) {
            authStore.clearFirebaseToken()
            configRepository.clearData()
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
        configScheduler.cancelScheduledSync()

        eventSyncManager.deleteSyncInfo()
        simNetwork.resetApiBaseUrl()
        configRepository.clearData()
        recentUserActivityManager.clearRecentActivity()

        deleteLocalData()

        Simber.tag(LoggingConstants.CrashReportTag.LOGOUT.name).i("Signed out")
    }

    private suspend fun deleteLocalData() {
        imageRepository.deleteStoredImages()
        eventRepository.deleteAll()
        enrolmentRecordRepository.deleteAll()
        scannerManager.deleteFirmwareFiles()
    }

}
