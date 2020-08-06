package com.simprints.id.services.sync

import com.simprints.id.services.sync.images.up.ImageUpSyncScheduler
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.sessionSync.SessionEventsSyncManager
import timber.log.Timber

class SyncSchedulerImpl(private val sessionEventsSyncManager: SessionEventsSyncManager,
                        private val eventSyncManager: EventSyncManager,
                        private val imageUpSyncScheduler: ImageUpSyncScheduler) : SyncManager {

    override fun scheduleBackgroundSyncs() {
        Timber.d("Background schedules synced")
        sessionEventsSyncManager.scheduleSessionsSync()
        eventSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
    }

    override fun cancelBackgroundSyncs() {
        Timber.d("Background schedules canceled")
        sessionEventsSyncManager.cancelSyncWorkers()
        eventSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
    }
}
