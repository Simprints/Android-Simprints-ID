package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.work.*
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.services.scheduledSync.people.common.*
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.services.scheduledSync.people.master.models.PeopleSyncState
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.scheduledSync.people.master.workers.PeopleSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import timber.log.Timber
import java.util.concurrent.TimeUnit

class PeopleSyncManagerImpl(private val ctx: Context,
                            private val peopleSyncStateProcessor: PeopleSyncStateProcessor,
                            private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
                            private val peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
                            private val peopleSyncCache: PeopleSyncCache) : PeopleSyncManager {

    companion object {
        const val SYNC_WORKER_REPEAT_INTERVAL = 15L
        val SYNC_WORKER_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun getLastSyncState(): LiveData<PeopleSyncState> =
        peopleSyncStateProcessor.getLastSyncState()

    override fun hasSyncEverRunBefore(): Boolean =
        wm.getAllPeopleSyncWorkersInfo().get().size > 0
    
    override fun sync() {
        Timber.tag(SYNC_LOG_TAG).d("Sync one time people master worker")
        wm.beginUniqueWork(
            MASTER_SYNC_SCHEDULER_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest()
        ).enqueue()
    }

    override fun scheduleSync() {
        Timber.tag(SYNC_LOG_TAG).d("Sync periodic people master worker")
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
        wm.cancelAllPeopleSyncWorkers()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(PeopleSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForOneTimeSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as OneTimeWorkRequest

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(
            PeopleSyncMasterWorker::class.java,
            SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForBackgroundSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as PeriodicWorkRequest

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    override suspend fun deleteSyncInfo() {
        peopleUpSyncScopeRepository.deleteAll()
        peopleDownSyncScopeRepository.deleteAll()
        peopleSyncCache.clearProgresses()
        peopleSyncCache.storeLastSuccessfulSyncTime(null)
    }
}
