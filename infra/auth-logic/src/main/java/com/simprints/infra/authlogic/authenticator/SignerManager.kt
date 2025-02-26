package com.simprints.infra.authlogic.authenticator

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGOUT
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class SignerManager @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val simNetwork: SimNetwork,
    private val imageRepository: ImageRepository,
    private val eventRepository: EventRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val licenseRepository: LicenseRepository,
    private val scannerManager: ScannerManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    val signedInProjectId: String
        get() = authStore.signedInProjectId

    suspend fun signIn(
        projectId: String,
        token: Token,
    ) = withContext(dispatcher) {
        try {
            // Store Firebase token so it can be used by ConfigManager
            authStore.storeFirebaseToken(token)
            configManager.refreshProject(projectId)
            // Only store credentials if all other calls succeeded. This avoids the undefined state
            // where credentials are store (i.e. user is considered logged in) but project configuration
            // is missing
            authStore.signedInProjectId = projectId
        } catch (e: Exception) {
            authStore.clearFirebaseToken()
            configManager.clearData()
            authStore.cleanCredentials()

            throw e
        }
    }

    suspend fun signOut() = withContext(dispatcher) {
        simNetwork.resetApiBaseUrl()
        configManager.clearData()
        recentUserActivityManager.clearRecentActivity()

        imageRepository.deleteStoredImages()
        eventRepository.deleteAll()
        enrolmentRecordRepository.deleteAll()
        scannerManager.deleteFirmwareFiles()
        licenseRepository.deleteCachedLicenses()

        authStore.cleanCredentials()
        authStore.clearFirebaseToken()

        Simber.i("Signed out", tag = LOGOUT)
    }
}
