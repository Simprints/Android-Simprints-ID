package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SYNC_MASTER_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_INTERVAL
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_UNIT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_SYNC_SCOPE_INPUT


class MasterSync(private val syncScopesBuilder: SyncScopesBuilder) {

    private val workerManager = WorkManager.getInstance()
    private val syncScope: SyncScope?
       get() = syncScopesBuilder.buildSyncScope()

    fun enqueueOneTimeSyncWorker() {
        syncScope?.let {
            workerManager.beginUniqueWork(
                SYNC_MASTER_WORKER_TAG,
                ExistingWorkPolicy.KEEP,
                buildOneTimeSyncWorkerRequest(it)
            ).enqueue()
        }
    }

    fun enqueuePeriodicSyncWorker() {
        syncScope?.let {
            workerManager.enqueueUniquePeriodicWork(
                SYNC_MASTER_WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                buildPeriodicDownSyncMasterWorker(it))
        }
    }

    private fun buildPeriodicDownSyncMasterWorker(it: SyncScope): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DownSyncMasterWorker>(SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setInputData(getDataForDownSyncMaster(it))
            .setConstraints(getSyncWorkerConstraints())
            .addTag(SYNC_MASTER_WORKER_TAG)
            .addTag(SYNC_WORKER_TAG)
            .addTag("oneTime")
            .build()

    fun dequeueAllSyncWorker() {
        workerManager.cancelAllWorkByTag(SYNC_WORKER_TAG)
    }

    private fun buildOneTimeSyncWorkerRequest(scope: SyncScope) =
        OneTimeWorkRequestBuilder<DownSyncMasterWorker>()
            .setInputData(getDataForDownSyncMaster(scope))
            .setConstraints(getSyncWorkerConstraints())
            .addTag(SYNC_MASTER_WORKER_TAG)
            .addTag(SYNC_WORKER_TAG)
            .addTag("periodic")
            .build()

    private fun getSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getDataForDownSyncMaster(scope: SyncScope) =
        workDataOf(SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(scope))

    fun isDownSyncRunning(): Boolean =
        syncScope?.let { it ->
            WorkManager.getInstance()
                .getStatusesByTag(SYNC_WORKER_TAG)
                .value?.find { it.state == State.RUNNING } != null
        } ?: false
}
