package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SimCoroutineWorker
import javax.inject.Inject

class DownSyncWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), DownSyncWorkerProgressReporter {

    companion object {
        const val DOWN_SYNC_WORKER_INPUT = "DOWN_SYNC_WORKER_INPUT"
        const val DOWN_SYNC_PROGRESS = "DOWN_SYNC_PROGRESS"
        const val DOWN_SYNC_OP = "DOWN_SYNC_OP"
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncTask: DownSyncTask

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val jsonForOp by lazy {
        inputData.getString(DOWN_SYNC_WORKER_INPUT)
            ?: throw IllegalArgumentException("input required")
    }

    override suspend fun doWork(): Result {
        return try {
            getComponent<DownSyncWorker> { it.inject(this) }
            val downSyncOperation = extractSubSyncScopeFromInput()

            logMessageForCrashReport<DownSyncWorker>("Preparing request for $downSyncOperation")

            execute(downSyncOperation)
        } catch (t: Throwable) {
            logFailure<DownSyncWorker>("Failed to prepare request.", t)

            t.printStackTrace()
            resultSetter.failure()
        }
    }

    private suspend fun execute(downSyncOperation: DownSyncOperation): Result {
        return try {
            downSyncTask.execute(downSyncOperation, this)
            logSuccess<DownSyncWorker>("Executing task done for $downSyncOperation")

            Result.success()
        } catch (t: Throwable) {
            logFailure<DownSyncWorker>("Failed on executing task for  $downSyncOperation", t)

            Result.retry()
        }
    }

    private fun extractSubSyncScopeFromInput(): DownSyncOperation {
        return JsonHelper.gson.fromJson(jsonForOp, DownSyncOperation::class.java)
    }

    override suspend fun reportCount(count: Int) {
        setProgressAsync(
            workDataOf(
                DOWN_SYNC_PROGRESS to count,
                DOWN_SYNC_OP to jsonForOp
            )
        )
    }
}
