package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.sync.ProjectConfigurationScheduler
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

internal class CancelBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val configScheduler: ProjectConfigurationScheduler,
) {

    operator fun invoke() {
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        configScheduler.cancelScheduledSync()
    }
}
