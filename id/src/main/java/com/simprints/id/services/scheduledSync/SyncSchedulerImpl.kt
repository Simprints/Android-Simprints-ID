package com.simprints.id.services.scheduledSync

import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import timber.log.Timber

class SyncSchedulerImpl(private val sessionEventsSyncManager: SessionEventsSyncManager,
                        private val subjectsSyncManager: SubjectsSyncManager,
                        private val imageUpSyncScheduler: ImageUpSyncScheduler) : SyncManager {

    override fun scheduleBackgroundSyncs() {
        Timber.d("Background schedules synced")
        sessionEventsSyncManager.scheduleSessionsSync()
        subjectsSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
    }

    override fun cancelBackgroundSyncs() {
        Timber.d("Background schedules canceled")
        sessionEventsSyncManager.cancelSyncWorkers()
        subjectsSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
    }
}
