package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.work.*
import androidx.work.WorkInfo.State.RUNNING
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.DOWNSYNC_MASTER_WORKER_TAG_PERIODIC
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_INTERVAL
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_UNIT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_SYNC_SCOPE_INPUT
import io.reactivex.Single

// Class to enqueue and dequeue DownSyncMasterWorker
class DownSyncManagerImpl(private val syncScopesBuilder: SyncScopesBuilder) : DownSyncManager {

    private val workerManager = WorkManager.getInstance()
    private val syncScope: SyncScope?
       get() = syncScopesBuilder.buildSyncScope()

    override fun enqueueOneTimeDownSyncMasterWorker() {
        syncScope?.let {
            workerManager.beginUniqueWork(
                DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME,
                ExistingWorkPolicy.KEEP,
                buildOneTimeDownSyncMasterWorker(it)
            ).enqueue()
        }
    }

    override fun enqueuePeriodicDownSyncMasterWorker() {
        syncScope?.let {
            workerManager.enqueueUniquePeriodicWork(
                DOWNSYNC_MASTER_WORKER_TAG_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                buildPeriodicDownSyncMasterWorker(it))
        }
    }

    override fun isDownSyncRunning(): Single<Boolean> = Single.fromCallable {
        syncScope?.let { it ->
            val workersInfo = WorkManager.getInstance().getWorkInfosByTag(SYNC_WORKER_TAG).get()
            workersInfo.find {
                it.state == RUNNING
            } != null
        } ?: false
    }

    override fun dequeueAllSyncWorker() {
        workerManager.cancelAllWorkByTag(SYNC_WORKER_TAG)
    }

    private fun buildPeriodicDownSyncMasterWorker(it: SyncScope): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DownSyncMasterWorker>(SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setInputData(getDataForDownSyncMasterWorker(it))
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME)
            .addTag(SYNC_WORKER_TAG)
            .addTag("oneTime")
            .build()

    private fun buildOneTimeDownSyncMasterWorker(scope: SyncScope) =
        OneTimeWorkRequestBuilder<DownSyncMasterWorker>()
            .setInputData(getDataForDownSyncMasterWorker(scope))
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
