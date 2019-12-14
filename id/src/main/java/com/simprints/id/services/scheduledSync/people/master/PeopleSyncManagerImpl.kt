package com.simprints.id.services.scheduledSync.people.master

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.work.*
import com.simprints.id.services.scheduledSync.people.down.models.SyncState
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.count.CountWorker.Companion.OUTPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.downsync.DownSyncWorker
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.master.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.sync.peopleDownSync.workers.master.DownSyncMasterWorker.Companion.TAG_LAST_SYNC_ID
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DownSyncManagerImpl(private val ctx: Context) : DownSyncManager {

    companion object {
        const val SYNC_WORKER_REPEAT_INTERVAL = 15L
        val SYNC_WORKER_REPEAT_UNIT = TimeUnit.MINUTES
    }

    override var lastSyncState: LiveData<SyncState?> =
        observerForLastDowSyncId().switchMap { lastSyncId ->
            observerForWorkersRelatedToLastSyncId(lastSyncId).switchMap { downSyncWorkers ->
                Timber.d("Sync - Received info for $lastSyncId: ${downSyncWorkers?.map { it.tags }}}")

                MutableLiveData<SyncState>().apply {
                    if (downSyncWorkers?.isNotEmpty() == true) {
                        val downSyncState = extractState(downSyncWorkers)
                        val total = extractDownSyncTotal(downSyncWorkers)
                        val progress = extractDownSyncProgress(downSyncWorkers)

                        this.postValue(SyncState(lastSyncId, progress, total, downSyncState))
                        Timber.d("Sync - Emitting ${this.value}")
                    }
                }
            }
        }


    private fun extractDownSyncProgress(downSyncWorkers: MutableList<WorkInfo>) =
        downSyncWorkers.firstOrNull {
            it.tags.contains(COUNT_SYNC_WORKER_TAG)
        }?.outputData?.getInt(OUTPUT_COUNT_WORKER_DOWN, -1).let { possibleProgress ->
            if (possibleProgress != null && possibleProgress < 0) {
                null
            } else {
                possibleProgress
            }
        }

    private fun extractDownSyncTotal(downSyncWorkers: MutableList<WorkInfo>) =
        downSyncWorkers.sumBy { it.progress.getInt(DownSyncWorker.DOWN_SYNC_PROGRESS, 0) }

    private fun extractState(downSyncWorkers: List<WorkInfo>): SyncState.State {
        val anyRunning = downSyncWorkers.any { it.state == WorkInfo.State.RUNNING }
        val anyEnqueued = downSyncWorkers.any { it.state == WorkInfo.State.ENQUEUED }
        val anyNotSucceeding = downSyncWorkers.any { it.state != WorkInfo.State.SUCCEEDED }
        return when {
            anyRunning -> {
                SyncState.State.RUNNING
            }
            anyEnqueued -> {
                SyncState.State.ENQUEUED
            }
            anyNotSucceeding -> {
                SyncState.State.FAILED
            }
            else -> {
                SyncState.State.COMPLETED
            }
        }
    }

    private fun observerForLastDowSyncId(): LiveData<String> {
        return wm.getWorkInfosByTagLiveData(MASTER_SYNC_SCHEDULERS).switchMap { workers ->
            Timber.d("Sync - Received info for schedulers: ${workers.map { it.tags }}")

            val activeWorkers = workers.filter { it.state == WorkInfo.State.SUCCEEDED }
            MutableLiveData<String>().apply {
                if (activeWorkers.isNotEmpty()) {
                    val lastScheduler = activeWorkers.findLast { it.outputData.getString(TAG_LAST_SYNC_ID) != null }
                    val lastSyncId = lastScheduler?.outputData?.getString(TAG_LAST_SYNC_ID)
                    if (!lastSyncId.isNullOrBlank()) {
                        this.postValue(lastSyncId)
                    }
                }
            }
        }
    }

    private fun observerForWorkersRelatedToLastSyncId(lastSyncId: String) =
        wm.getWorkInfosByTagLiveData(lastSyncId)

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

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

    override fun stop() {
        wm.cancelAllWorkByTag(SYNC_WORKER_TAG)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(DownSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(MASTER_SYNC_SCHEDULERS)
            .addTag(MASTER_SYNC_SCHEDULER_ONE_TIME)
            .build()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(DownSyncMasterWorker::class.java, SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(MASTER_SYNC_SCHEDULERS)
            .addTag(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)
            .build()

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}
