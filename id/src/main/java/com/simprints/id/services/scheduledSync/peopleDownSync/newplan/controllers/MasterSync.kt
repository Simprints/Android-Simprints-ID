package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.controllers

import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SubCountWorker.Companion.SUBCOUNT_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SubDownSyncWorker.Companion.SUBDOWNSYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker.Companion.SYNC_WORKER_REPEAT_INTERVAL
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker.Companion.SYNC_WORKER_REPEAT_UNIT
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker.Companion.SYNC_WORKER_SYNC_SCOPE_INPUT
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.SyncWorker.Companion.SYNC_WORKER_TAG


/**
 * Fabio - MasterSync: class to enqueue the workers
 * - enqueue OneTime SyncWorker
 * - enqueue Periodic SyncWorker
 * - enqueue OneTime CountWorker
 * - dequeue workers
 */

class MasterSync(private val syncScopesBuilder: SyncScopesBuilder) {

    private val workerManager = WorkManager.getInstance()
    private val syncScope:SyncScope?
       get() = syncScopesBuilder.buildSyncScope()

    fun enqueueOneTimeSyncWorker() {
        val syncScope = syncScopesBuilder.buildSyncScope()
        syncScope?.let {
            workerManager.beginUniqueWork(
                SYNC_WORKER_TAG,
                ExistingWorkPolicy.KEEP,
                buildOneTimeSyncWorkerRequest(it)
            ).enqueue()
        }
    }

    fun enqueuePeriodicSyncWorker() {
        syncScope?.let {
            PeriodicWorkRequestBuilder<SyncWorker>(SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
                .setConstraints(getSyncWorkerConstraints())
                .setInputData(workDataOf(SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(it)))
                .addTag(SYNC_WORKER_TAG)
                .build().also { periodicWorker ->
                    workerManager.enqueueUniquePeriodicWork(SYNC_WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, periodicWorker)
                }
        }
    }

    fun dequeueAllSyncWorker() {
        workerManager.cancelAllWorkByTag(SUBCOUNT_WORKER_TAG)
        workerManager.cancelAllWorkByTag(SUBDOWNSYNC_WORKER_TAG)
        workerManager.cancelAllWorkByTag(SYNC_WORKER_TAG)
    }


    private fun buildOneTimeSyncWorkerRequest(scope: SyncScope) =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf(SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(scope)))
            .setConstraints(getSyncWorkerConstraints())
            .addTag(SYNC_WORKER_TAG)
            .build()

    private fun getSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun isDownSyncRunning(): Boolean =
        syncScope?.let { it ->
            WorkManager.getInstance()
                .getStatusesForUniqueWork(SyncWorker.getSyncChainWorkersUniqueNameForSync(it))
                .value?.find { it.state == State.RUNNING } != null
        } ?: false
}
