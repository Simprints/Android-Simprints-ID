package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.controllers

import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.CountWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.CountWorker.Companion.COUNT_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker.Companion.SYNC_WORKER_REPEAT_INTERVAL
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker.Companion.SYNC_WORKER_REPEAT_UNIT
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker.Companion.SYNC_WORKER_TAG
import com.simprints.id.services.sync.SyncTaskParameters

/**
 * Fabio - MasterSync: class to enqueue the workers
 * - enqueue OneTime SyncWorker
 * - enqueue Periodic SyncWorker
 * - enqueue OneTime CountWorker
 * - dequeue workers
 */

class MasterSync {

    private val workerManager = WorkManager.getInstance()

    fun enqueueOneTimeSyncWorker(syncParams: SyncTaskParameters) {
        workerManager.beginUniqueWork(
            SYNC_WORKER_TAG,
            ExistingWorkPolicy.KEEP,
            buildOneTimeSyncWorkerRequest(syncParams)
        ).enqueue()
    }

    fun enqueuePeriodicSyncWorker(syncParams: SyncTaskParameters) {
        PeriodicWorkRequestBuilder<SyncWorker>(SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setConstraints(getSyncWorkerConstraints())
            .addTag(SYNC_WORKER_TAG)
            .setInputData(syncParams.toData())
            .build().also {
                workerManager.enqueueUniquePeriodicWork(SYNC_WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, it)
            }
    }

    fun enqueueOneTimeCountWorker(syncParams: SyncTaskParameters) {
        workerManager.beginUniqueWork(
            COUNT_WORKER_TAG,
            ExistingWorkPolicy.KEEP,
            buildOneTimeSyncWorkerRequest(syncParams)
        ).enqueue()
    }

    fun dequeueAllSyncWorker() {
        //StopShip - implement it.
    }

    private fun buildOneTimeCountWorkerRequest(syncParams: SyncTaskParameters) =
        OneTimeWorkRequestBuilder<CountWorker>()
            .setConstraints(getCountWorkerConstraints())
            .addTag(COUNT_WORKER_TAG)
            .setInputData(syncParams.toData())
            .build()

    private fun buildOneTimeSyncWorkerRequest(syncParams: SyncTaskParameters) =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(syncParams.toData())
            .setConstraints(getSyncWorkerConstraints())
            .addTag(SYNC_WORKER_TAG)
            .build()

    private fun getCountWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getSyncWorkerConstraints() =
        Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
}
