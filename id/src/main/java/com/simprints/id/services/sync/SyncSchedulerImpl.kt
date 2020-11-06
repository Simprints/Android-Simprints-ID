package com.simprints.id.services.sync

import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import timber.log.Timber

class SyncSchedulerImpl(private val eventSyncManager: EventSyncManager,
                        private val imageUpSyncScheduler: ImageUpSyncScheduler) : SyncManager {

    override fun scheduleBackgroundSyncs() {
        Timber.d("Background schedules synced")
        eventSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
    }

    override fun cancelBackgroundSyncs() {
        Timber.d("Background schedules canceled")
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
    }
}
