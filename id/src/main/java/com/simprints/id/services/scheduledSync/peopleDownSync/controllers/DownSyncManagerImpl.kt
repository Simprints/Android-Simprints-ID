package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.master.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.master.DownSyncMasterWorker.Companion.LAST_SYNC_ID
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DownSyncManagerImpl(private val ctx: Context) : DownSyncManager {

    companion object {
        const val MASTER_SYNC_SCHEDULER_ONE_TIME = "MASTER_SYNC_SCHEDULER_ONE_TIME"
        const val MASTER_SYNC_SCHEDULER_PERIODIC_TIME = "MASTER_SYNC_SCHEDULER_PERIODIC_TIME"
        const val MASTER_SYNC_SCHEDULER = "MASTER_SYNC_SCHEDULER"

        const val SYNC_WORKER_TAG = "SYNC_WORKER_TAG"
        const val DOWN_SYNC_WORKER_TAG = "DOWN_SYNC_WORKER_TAG"
        const val COUNT_SYNC_WORKER_TAG = "COUNT_SYNC_WORKER_TAG"

        const val LAST_SYNC_SHARED_KEY = "LAST_SYNC_SHARED_KEY"
        const val SYNC_WORKER_REPEAT_INTERVAL = 15L
        val SYNC_WORKER_REPEAT_UNIT = TimeUnit.MINUTES
    }

    fun <A, B> LiveData<A>.combine(other: LiveData<B>): PairLiveData<A, B> {
        return PairLiveData(this, other)
    }

    class PairLiveData<A, B>(first: LiveData<A>, second: LiveData<B>) : MediatorLiveData<Pair<A?, B?>>() {
        init {
            addSource(first) { value = it to second.value }
            addSource(second) { value = first.value to it }
        }
    }

    override var lastSyncState: LiveData<SyncState?> =
        wm.getWorkInfosByTagLiveData(MASTER_SYNC_SCHEDULER).map { workers ->
            Timber.d("Received info for schedulers: ${workers.map { it.tags }}")
            val activeWorkers = workers.filter { it.state == WorkInfo.State.SUCCEEDED }
            return@map if (activeWorkers.isNotEmpty()) {
                val lastScheduler = activeWorkers.findLast { it.outputData.getString(LAST_SYNC_ID) != null }
                val lastSyncId = lastScheduler?.outputData?.getString(LAST_SYNC_ID)
                lastSyncId
            } else {
                null
            }
        }.switchMap { lastSyncId ->
            lastSyncId?.let {
                Transformations.map(wm.getWorkInfosByTagLiveData(it)
                    .combine(MutableLiveData<String>().also { liveData -> liveData.value = it })) { syncIdAndWorkers ->

                    val downWorkers = syncIdAndWorkers.first
                    val lastSyncId = syncIdAndWorkers.second

                    Timber.d("Received info for $lastSyncId: ${downWorkers?.map { it.tags }}}")

                    return@map if (downWorkers != null && lastSyncId != null) {
                        val anyRunning = downWorkers.firstOrNull { it.state == WorkInfo.State.RUNNING } != null
                        val anyEnqueued = downWorkers.firstOrNull { it.state == WorkInfo.State.ENQUEUED } != null
                        val anyNotSucceeding = downWorkers.firstOrNull { it.state != WorkInfo.State.SUCCEEDED } != null

                        SyncState(lastSyncId, anyRunning, anyEnqueued, anyNotSucceeding)
                    } else {
                        null
                    }.also {
                        Timber.d("Emitting $it")
                    }
                }
            } ?: MutableLiveData<SyncState>()
        }

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
        wm.cancelAllWorkByTag(MASTER_SYNC_SCHEDULER)
    }

    override fun stop() {
        wm.cancelAllWorkByTag(SYNC_WORKER_TAG)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildOneTimeRequest(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(DownSyncMasterWorker::class.java)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(MASTER_SYNC_SCHEDULER)
            .addTag(MASTER_SYNC_SCHEDULER_ONE_TIME)
            .addTag(SYNC_WORKER_TAG)
            .build()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun buildPeriodicRequest(): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(DownSyncMasterWorker::class.java, SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(MASTER_SYNC_SCHEDULER)
            .addTag(MASTER_SYNC_SCHEDULER_PERIODIC_TIME)
            .addTag(SYNC_WORKER_TAG)
            .build()

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}
