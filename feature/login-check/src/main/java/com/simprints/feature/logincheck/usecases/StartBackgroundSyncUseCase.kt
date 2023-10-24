package com.simprints.feature.logincheck.usecases

import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import javax.inject.Inject

internal class StartBackgroundSyncUseCase @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val configManager: ConfigManager,
) {

    suspend operator fun invoke() {
        eventSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
        configManager.scheduleSyncConfiguration()

        val frequency = configManager.getProjectConfiguration().synchronization.frequency
        if (frequency == SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START) {
            eventSyncManager.sync()
        }
    }
}
