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
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import timber.log.Timber
import javax.inject.Inject

class PeopleDownSyncDownloaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val PROGRESS_DOWN_SYNC_OPS = "PROGRESS_DOWN_SYNC_OPS"
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

            logMessageForCrashReport<PeopleDownSyncDownloaderWorker>("Sync - Preparing request for $downSyncOperation")

            execute(downSyncOperation)
        } catch (t: Throwable) {
            logFailure<PeopleDownSyncDownloaderWorker>("Sync - Failed to prepare request.", t)

            t.printStackTrace()
            resultSetter.failure()
        }
    }

    private suspend fun execute(downSyncOperation: PeopleDownSyncOperation): Result {
        return try {
            peopleDownSyncDownloaderTask.execute(downSyncOperation, this)
            logSuccess<PeopleDownSyncDownloaderWorker>("Sync - Executing task done for $downSyncOperation")

            resultSetter.success()
        } catch (t: Throwable) {
            logFailure<PeopleDownSyncDownloaderWorker>("Sync - Failed on executing task for  $downSyncOperation", t)

            resultSetter.retry()
        }
    }

    private fun extractSubSyncScopeFromInput(): PeopleDownSyncOperation {
        return JsonHelper.gson.fromJson(jsonForOp, PeopleDownSyncOperation::class.java)
    }

    override suspend fun reportCount(count: Int) {
        Timber.d("Sync - Progress: $count")
        setProgressAsync(
            workDataOf(
                PROGRESS_DOWN_SYNC to count)
        )
    }
}

fun WorkInfo.extractSyncProgress(): Int =
    this.progress.getInt(PROGRESS_DOWN_SYNC, 0)
