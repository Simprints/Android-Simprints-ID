package com.simprints.infra.sync.config.usecase

import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

internal class LogoutUseCase @Inject constructor(
    private val syncOrchestrator: SyncOrchestrator,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val eventSyncManager: EventSyncManager,
    private val authManager: AuthManager,
) {

    suspend operator fun invoke() {
        imageUpSyncScheduler.cancelImageUpSync()
        syncOrchestrator.cancelBackgroundWork()
        eventSyncManager.cancelScheduledSync()
        eventSyncManager.deleteSyncInfo()
        authManager.signOut()
    }
}
