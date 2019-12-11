package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.syncscope.domain.DownSyncOperation
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.master.DownSyncMasterWorker.Companion.MAX_ATTEMPTS
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync.DownSyncTask
import timber.log.Timber
import javax.inject.Inject

class DownSyncWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val DOWN_SYNC_WORKER_INPUT = "DOWN_SYNC_WORKER_INPUT"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncTask: DownSyncTask

    override suspend fun doWork(): Result {
        getComponent<DownSyncWorker> { it.inject(this) }

        val downSyncOperation = extractSubSyncScopeFromInput(inputData)
        logMessageForCrashReport("Making downsync request for $downSyncOperation")

        return if(runAttemptCount < MAX_ATTEMPTS) {
            try {
                downSyncTask.execute(downSyncOperation)
                logSuccess(downSyncOperation)
                Result.success()
            } catch (t: Throwable) {
                logFailure(downSyncOperation, t)
                Result.retry()
            }
        } else {
            Result.failure()
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

    private fun extractSubSyncScopeFromInput(inputData: Data): DownSyncOperation {
        val input = inputData.getString(DOWN_SYNC_WORKER_INPUT)
            ?: throw IllegalArgumentException("input required")
        return JsonHelper.gson.fromJson(input, DownSyncOperation::class.java)
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)
}
