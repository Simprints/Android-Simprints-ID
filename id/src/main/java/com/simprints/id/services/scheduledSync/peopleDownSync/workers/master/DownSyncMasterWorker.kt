package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import android.content.Context
import androidx.work.*
import androidx.work.WorkRequest.Builder
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.syncscope.DownSyncScopeRepository
import com.simprints.id.data.db.syncscope.domain.DownSyncOperation
import com.simprints.id.data.db.syncscope.domain.DownSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManagerImpl
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManagerImpl.Companion.SYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncWorker.Companion.DOWN_SYNC_WORKER_INPUT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync.DownSyncMasterWorker.Companion.MIN_BACKOFF_MILLIS
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DownSyncMasterWorker(private val appContext: Context,
                           params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    @Inject lateinit var downSyncScopeRepository: DownSyncScopeRepository
    @Inject lateinit var downSyncManager: DownSyncManager

    companion object {
        private const val MAX_BACKOFF_MILLIS = (5 * 60 * 1000) // 5 minutes
        const val MIN_BACKOFF_MILLIS = 15L //15 seconds

        const val MAX_ATTEMPTS = MAX_BACKOFF_MILLIS / MIN_BACKOFF_MILLIS
    }

    private val wm: WorkManager
        get() = WorkManager.getInstance(appContext)

    private val downSyncScope: DownSyncScope
        get() = downSyncScopeRepository.getDownSyncScope()

    override suspend fun doWork(): Result {
        getComponent<DownSyncMasterWorker> { it.inject(this) }

        val operations = downSyncScope.getDownSyncOperations()
        if (!isSyncRunning()) {
            val uniqueSyncID = UUID.randomUUID().toString()
            downSyncManager.lastSyncId = uniqueSyncID
            val chain = buildChain(uniqueSyncID, operations)
            wm.enqueue(chain.toList())
        }

        return Result.success()
    }

    private fun buildChain(uniqueSyncID: String, operations: Array<DownSyncOperation>): List<WorkRequest> =
        operations.map { buildDownSyncWorker(uniqueSyncID, it) } + listOf(buildCountWorker(uniqueSyncID))

    private fun isSyncRunning(): Boolean {
        val infoLiveData = wm.getWorkInfosByTagLiveData(SYNC_WORKER_TAG)
        val value = infoLiveData.value
        return value?.firstOrNull { it.state == WorkInfo.State.RUNNING } != null
    }

    private fun buildDownSyncWorker(uniqueSyncID: String, downSyncOperation: DownSyncOperation): WorkRequest =
        OneTimeWorkRequest.Builder(DownSyncWorker::class.java)
            .setInputData(workDataOf(DOWN_SYNC_WORKER_INPUT to JsonHelper.gson.toJson(downSyncOperation)))
            .setDownSyncWorker(uniqueSyncID, getDownSyncWorkerConstraints())
            .build()

    private fun buildCountWorker(uniqueSyncID: String): WorkRequest =
        OneTimeWorkRequest.Builder(DownSyncWorker::class.java)
            .setDownSyncWorker(uniqueSyncID, getDownSyncWorkerConstraints())
            .build()

    private fun getDownSyncWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}

private fun Builder<*, *>.setDownSyncWorker(uniqueSyncID: String, constraints: Constraints) =
    this.setConstraints(constraints)
        .addTag(uniqueSyncID)
        .addTag(SYNC_WORKER_TAG)
        .addTag(DownSyncManagerImpl.COUNT_SYNC_WORKER_TAG)
        .setBackoffCriteria(BackoffPolicy.LINEAR, MIN_BACKOFF_MILLIS, TimeUnit.SECONDS)
