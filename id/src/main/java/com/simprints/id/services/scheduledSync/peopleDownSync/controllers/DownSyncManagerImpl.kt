package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncState
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.DOWNSYNC_MASTER_WORKER_TAG_PERIODIC
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.SUBDOWNSYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.SYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_INTERVAL
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_UNIT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_SYNC_SCOPE_INPUT

// Class to enqueue and dequeue DownSyncMasterWorker
class DownSyncManagerImpl(private val syncScopesBuilder: SyncScopesBuilder) : DownSyncManager {

    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    override fun enqueueOneTimeDownSyncMasterWorker() {
        syncScope?.let {
            WorkManager.getInstance().beginUniqueWork(
                DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME,
                ExistingWorkPolicy.KEEP,
                buildOneTimeDownSyncMasterWorker(it)
            ).enqueue()
        }
    }

    override fun enqueuePeriodicDownSyncMasterWorker() {
        syncScope?.let {
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                DOWNSYNC_MASTER_WORKER_TAG_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                buildPeriodicDownSyncMasterWorker(it))
        }
    }

    override fun onSyncStateUpdated(): LiveData<SyncState> {
        val liveDataSyncWorkersInfo = WorkManager.getInstance().getWorkInfosByTagLiveData(SUBDOWNSYNC_WORKER_TAG)

        return Transformations.map(liveDataSyncWorkersInfo) {

            val isSyncCounting = it.any { workInfo -> workInfo.state == WorkInfo.State.BLOCKED }
            val isSyncRunning = it.any { workInfo -> workInfo.state == WorkInfo.State.RUNNING }
            when {
                isSyncCounting -> SyncState.CALCULATING
                isSyncRunning -> SyncState.RUNNING
                else -> SyncState.NOT_RUNNING
            }
        }
    }

    override fun dequeueAllSyncWorker() {
        WorkManager.getInstance().cancelAllWorkByTag(SYNC_WORKER_TAG)
    }

    override fun buildPeriodicDownSyncMasterWorker(syncScope: SyncScope): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DownSyncMasterWorker>(SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setInputData(getDataForDownSyncMasterWorker(syncScope))
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME)
            .addTag(SYNC_WORKER_TAG)
            .addTag("oneTime")
            .build()


    override fun buildOneTimeDownSyncMasterWorker(syncScope: SyncScope) =
        OneTimeWorkRequestBuilder<DownSyncMasterWorker>()
            .setInputData(getDataForDownSyncMasterWorker(syncScope))
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME)
            .addTag(SYNC_WORKER_TAG)
            .addTag("periodic")
            .build()

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getDataForDownSyncMasterWorker(scope: SyncScope) =
        workDataOf(SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(scope))
}
