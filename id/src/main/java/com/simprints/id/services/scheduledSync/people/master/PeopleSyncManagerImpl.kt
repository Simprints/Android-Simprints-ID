package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.work.*
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_PEOPLE_SYNC_ALL_WORKERS
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncMasterWorker.Companion.TAG_SCHEDULED_AT
import java.util.*
import java.util.concurrent.TimeUnit

class PeopleSyncManagerImpl(private val ctx: Context,
                            private val peopleSyncStateProcessor: PeopleSyncStateProcessor) : PeopleSyncManager {

    companion object {
        const val SYNC_WORKER_REPEAT_INTERVAL = 15L
        val SYNC_WORKER_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun getLastSyncState(): LiveData<PeopleSyncState> =
        peopleSyncStateProcessor.getLastSyncState()

    override fun sync() {
        wm.beginUniqueWork(
            MASTER_SYNC_SCHEDULER_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest()
        ).enqueue()
    }

    override fun scheduleSync() {
        wm.enqueueUniquePeriodicWork(
            MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildPeriodicRequest())
    }

    override fun cancelScheduledSync() {
        wm.cancelAllWorkByTag(MASTER_SYNC_SCHEDULERS)
    }

    override fun cancelAndRescheduleSync() {
        cancelScheduledSync()
        stop()
        wm.pruneWork()

        scheduleSync()
    }

    override fun stop() {
        wm.cancelAllWorkByTag(TAG_PEOPLE_SYNC_ALL_WORKERS)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(MASTER_SYNC_SCHEDULERS)
            .addTag(MASTER_SYNC_SCHEDULER_ONE_TIME)
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .build()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(PeopleSyncMasterWorker::class.java, SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(MASTER_SYNC_SCHEDULERS)
            .addTag(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)
            .addTag("${TAG_SCHEDULED_AT}${Date().time}")
            .build()

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}
