package com.simprints.id.services.scheduledSync.people.down.controllers.builder

import androidx.work.*
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
import com.simprints.id.services.scheduledSync.people.down.controllers.manager.DownSyncManagerImpl
import com.simprints.id.services.scheduledSync.people.down.workers.CountWorker
import com.simprints.id.services.scheduledSync.people.down.workers.DownSyncWorker
import com.simprints.id.services.scheduledSync.people.master.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.people.up.controllers.DownSyncWorkerBuilder
import java.util.*
import java.util.concurrent.TimeUnit

class DownSyncWorkerBuilderImpl : DownSyncWorkerBuilder {

    override fun buildDownSyncWorkerChain(uniqueSyncID: String, operations: List<DownSyncOperation>): List<WorkRequest> =
        operations.map { buildDownSyncWorkers(uniqueSyncID, it) } + listOf(buildCountWorker(uniqueSyncID))


    private fun buildDownSyncWorkers(uniqueSyncID: String, downSyncOperation: DownSyncOperation): WorkRequest =
        OneTimeWorkRequest.Builder(DownSyncWorker::class.java)
            .setInputData(workDataOf(DownSyncWorker.DOWN_SYNC_WORKER_INPUT to JsonHelper.gson.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, getDownSyncWorkerConstraints())
            .addTag(DownSyncManagerImpl.DOWN_SYNC_WORKER_TAG)
            .build()

    private fun buildCountWorker(uniqueSyncID: String): WorkRequest =
        OneTimeWorkRequest.Builder(CountWorker::class.java)
            .setDownSyncWorker(uniqueSyncID, getDownSyncWorkerConstraints())
            .addTag(DownSyncManagerImpl.COUNT_SYNC_WORKER_TAG)
            .build()

    private fun getDownSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun WorkRequest.Builder<*, *>.setDownSyncWorker(uniqueSyncID: String, constraints: Constraints) =
        this.setConstraints(constraints)
            .addTag(uniqueSyncID)
            .addTag(DownSyncManagerImpl.SYNC_WORKER_TAG)
            .addTag("${DownSyncMasterWorker.TAG_SCHEDULED_AT}${Date().time}")
            .setBackoffCriteria(BackoffPolicy.LINEAR, DownSyncMasterWorker.MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)

}
