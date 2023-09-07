package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

class CancelBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val configManager: ConfigManager,
) {

    operator fun invoke() {
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        configManager.cancelScheduledSyncConfiguration()
    }
}
