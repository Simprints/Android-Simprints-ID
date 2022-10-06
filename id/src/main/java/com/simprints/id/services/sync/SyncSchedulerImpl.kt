package com.simprints.id.services.sync

import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.Simber

class SyncSchedulerImpl(
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
