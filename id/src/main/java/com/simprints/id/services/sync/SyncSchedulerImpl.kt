package com.simprints.id.services.sync

import com.simprints.id.services.config.RemoteConfigScheduler
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.logging.Simber

class SyncSchedulerImpl(private val eventSyncManager: EventSyncManager,
                        private val imageUpSyncScheduler: ImageUpSyncScheduler,
                        private val remoteConfigScheduler: RemoteConfigScheduler
) : SyncManager {

    override fun scheduleBackgroundSyncs() {
        Simber.d("Background schedules synced")
        eventSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
        remoteConfigScheduler.scheduleSync()
    }

    override fun cancelBackgroundSyncs() {
        Simber.d("Background schedules canceled")
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
        remoteConfigScheduler.cancelScheduledSync()
    }
}
