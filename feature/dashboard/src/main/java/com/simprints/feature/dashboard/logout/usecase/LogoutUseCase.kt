package com.simprints.feature.dashboard.logout.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val configScheduler: ProjectConfigurationScheduler,
    private val securityStateScheduler: SecurityStateScheduler,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val eventSyncManager: EventSyncManager,
    private val authManager: AuthManager,
) {

    suspend operator fun invoke() {
        // Cancel all background sync
        securityStateScheduler.cancelSecurityStateCheck()
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        configScheduler.cancelScheduledSync()

        eventSyncManager.deleteSyncInfo()
        authManager.signOut()
    }
}
