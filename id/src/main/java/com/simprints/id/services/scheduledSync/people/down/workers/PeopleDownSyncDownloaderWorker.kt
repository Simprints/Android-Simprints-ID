package com.simprints.id.services.scheduledSync.people.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import javax.inject.Inject

class PeopleDownSyncDownloaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val OUTPUT_DOWN_SYNC = "OUTPUT_DOWN_SYNC"
    }

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var peopleDownSyncDownloaderTask: PeopleDownSyncDownloaderTask

    private val jsonForOp by lazy {
        inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
    }

    override suspend fun doWork(): Result {
        return try {
            getComponent<PeopleDownSyncDownloaderWorker> { it.inject(this) }
            val downSyncOperation = extractSubSyncScopeFromInput()
            crashlyticsLog("Preparing downSync request for $downSyncOperation")

            execute(downSyncOperation)
        } catch (t: Throwable) {
            logFailure(t)

            resultSetter.failure()
        }
    }

    private suspend fun execute(downSyncOperation: PeopleDownSyncOperation): Result {
        return try {
            val totalDownloaded = peopleDownSyncDownloaderTask.execute(downSyncOperation, this)
            logSuccess("DownSync done for $downSyncOperation: $totalDownloaded downloaded")

            resultSetter.success(workDataOf(OUTPUT_DOWN_SYNC to totalDownloaded))
        } catch (t: Throwable) {
            logFailure(t)

            resultSetter.retry()
        }
    }

    private fun extractSubSyncScopeFromInput(): PeopleDownSyncOperation {
        return JsonHelper.gson.fromJson(jsonForOp, PeopleDownSyncOperation::class.java)
    }

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_DOWN_SYNC to count)
        )
    }

    private fun logFailure(t: Throwable) =
        logFailure<PeopleDownSyncDownloaderWorker>(t)

    private fun logSuccess(message: String) =
        logSuccess<PeopleDownSyncDownloaderWorker>(message)

    private fun crashlyticsLog(message: String) =
        crashlyticsLog<PeopleDownSyncDownloaderWorker>(message)
}

fun WorkInfo.extractDownSyncProgress(): Int? {
    val progress = this.progress.getInt(PROGRESS_DOWN_SYNC, -1)
    return if (progress < 0) {
        val output = this.outputData.getInt(OUTPUT_DOWN_SYNC, -1)
        if (output < 0) {
            null
        } else {
            output
        }
    } else {
        progress
    }
}
