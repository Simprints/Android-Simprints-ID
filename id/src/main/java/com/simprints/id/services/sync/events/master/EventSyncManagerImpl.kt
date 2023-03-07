package com.simprints.id.services.sync.events.master

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import com.simprints.id.BuildConfig
import com.simprints.id.services.sync.events.common.*
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULERS
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_ONE_TIME
import com.simprints.id.services.sync.events.master.workers.EventSyncMasterWorker.Companion.MASTER_SYNC_SCHEDULER_PERIODIC_TIME
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// TODO move into its own module
class EventSyncManagerImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val eventSyncStateProcessor: EventSyncStateProcessor,
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val upSyncScopeRepo: EventUpSyncScopeRepository,
    private val eventSyncCache: EventSyncCache,
) : EventSyncManager, com.simprints.feature.dashboard.main.sync.EventSyncManager {

    companion object {
        private const val SYNC_REPEAT_INTERVAL = BuildConfig.SYNC_PERIODIC_WORKER_INTERVAL_MINUTES
        val SYNC_REPEAT_UNIT = TimeUnit.MINUTES
    }

    private val wm = WorkManager.getInstance(ctx)

    override fun getLastSyncState(): LiveData<EventSyncState> =
        eventSyncStateProcessor.getLastSyncState()

    override fun hasSyncEverRunBefore(): Boolean =
        wm.getAllSubjectsSyncWorkersInfo().get().size > 0

    override fun sync() {
        Simber.tag(SYNC_LOG_TAG).d("[SCHEDULER] One time events master worker")

        wm.beginUniqueWork(
            MASTER_SYNC_SCHEDULER_ONE_TIME,
            ExistingWorkPolicy.KEEP,
            buildOneTimeRequest()
        ).enqueue()
    }

    override fun scheduleSync() {
        Simber.tag(SYNC_LOG_TAG).d("[SCHEDULER] Periodic events master worker")

        wm.enqueueUniquePeriodicWork(
            MASTER_SYNC_SCHEDULER_PERIODIC_TIME,
            ExistingPeriodicWorkPolicy.KEEP,
            buildPeriodicRequest()
        )

    }

    override fun cancelScheduledSync() {
        wm.cancelAllWorkByTag(MASTER_SYNC_SCHEDULERS)
        stop()
    }

    override fun stop() {
        wm.cancelAllSubjectsSyncWorkers()
    }

    private fun cleanScheduledHistory() {
        wm.pruneWork()
    }

    private fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(EventSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTagForSyncMasterWorkers()
            .addTagForOneTimeSyncMasterWorker()
            .addTagForScheduledAtNow()
            .build() as OneTimeWorkRequest

    private fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(
            EventSyncMasterWorker::class.java,
            SYNC_REPEAT_INTERVAL,
            SYNC_REPEAT_UNIT
        )
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
        downSyncScopeRepository.deleteAll()
        upSyncScopeRepo.deleteAll()
        eventSyncCache.clearProgresses()
        eventSyncCache.storeLastSuccessfulSyncTime(null)
        cleanScheduledHistory()
    }
}
