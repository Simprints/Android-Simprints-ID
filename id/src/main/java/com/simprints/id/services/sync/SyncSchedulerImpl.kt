package com.simprints.id.services.sync

import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber
import javax.inject.Inject

class SyncSchedulerImpl @Inject constructor(
    private val eventSyncManager: EventSyncManager,
    private val imageUpSyncScheduler: ImageUpSyncScheduler,
    private val configManager: ConfigManager,
) : SyncManager {

    override fun scheduleBackgroundSyncs() {
        Simber.d("Background schedules synced")
        eventSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
        configManager.scheduleSyncConfiguration()
    }

    override fun cancelBackgroundSyncs() {
        Simber.d("Background schedules canceled")
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        configManager.cancelScheduledSyncConfiguration()
    }
}
