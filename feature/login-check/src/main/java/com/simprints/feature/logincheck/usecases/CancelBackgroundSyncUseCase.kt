package com.simprints.feature.logincheck.usecases

import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.sync.SyncOrchestrator
import javax.inject.Inject

internal class CancelBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val syncOrchestrator: SyncOrchestrator,
) {

    suspend operator fun invoke() {
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        syncOrchestrator.cancelBackgroundWork()
    }
}
