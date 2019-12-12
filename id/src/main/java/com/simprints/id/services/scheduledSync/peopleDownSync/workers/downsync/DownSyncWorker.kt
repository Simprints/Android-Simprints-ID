package com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.down_sync_info.domain.DownSyncOperation
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SimCoroutineWorker
import timber.log.Timber
import javax.inject.Inject

class DownSyncWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), DownSyncWorkerProgressReporter {

    companion object {
        const val DOWN_SYNC_WORKER_INPUT = "DOWN_SYNC_WORKER_INPUT"
        const val DOWN_SYNC_PROGRESS = "DOWN_SYNC_PROGRESS"
        const val DOWN_SYNC_OP = "DOWN_SYNC_OP"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncTask: DownSyncTask

    private val jsonForOp by lazy {
        inputData.getString(DOWN_SYNC_WORKER_INPUT)
            ?: throw IllegalArgumentException("input required")
    }

    override suspend fun doWork(): Result {
        getComponent<DownSyncWorker> { it.inject(this) }

        val downSyncOperation = extractSubSyncScopeFromInput()
        Timber.d("DownSyncWorker - started: $downSyncOperation")

        logMessageForCrashReport("Making downsync request for $downSyncOperation")

        return try {
            downSyncTask.execute(downSyncOperation, this)
            logSuccess(downSyncOperation)
            Timber.d("DownSyncWorker - done for $downSyncOperation")

            Result.success()
        } catch (t: Throwable) {
            logFailure(downSyncOperation, t)
            Timber.d("DownSyncWorker - failed for $downSyncOperation")

            Result.retry()
        }
    }

    private fun logFailure(downSyncOperation: DownSyncOperation, t: Throwable) {
        Timber.e(t)
        crashReportManager.logExceptionOrSafeException(t)
        showToastForDebug<DownSyncOperation>(downSyncOperation, Result.failure())
    }

    private fun logSuccess(downSyncOperation: DownSyncOperation) {
        logMessageForCrashReport("DownSyncing for $downSyncOperation")
        showToastForDebug<DownSyncOperation>(downSyncOperation, Result.success())
    }

    private fun extractSubSyncScopeFromInput(): DownSyncOperation {
        return JsonHelper.gson.fromJson(jsonForOp, DownSyncOperation::class.java)
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)

    override suspend fun reportCount(count: Int) {
        setProgressAsync(
            workDataOf(
                DOWN_SYNC_PROGRESS to count,
                DOWN_SYNC_OP to jsonForOp
            )
        )
    }
}
