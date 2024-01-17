package com.simprints.feature.dashboard.logout.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val configScheduler: ProjectConfigurationScheduler,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val eventSyncManager: EventSyncManager,
    private val authManager: AuthManager,
) {

    suspend operator fun invoke() {
        // Cancel all background sync
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        configScheduler.cancelProjectSync()
        configScheduler.cancelDeviceSync()

        eventSyncManager.deleteSyncInfo()
        authManager.signOut()
    }
}
