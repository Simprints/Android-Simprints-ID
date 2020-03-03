package com.simprints.id.services.scheduledSync.people.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperation
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.common.WorkerProgressCountReporter
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.scheduledSync.people.down.workers.PeopleDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.scheduledSync.people.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PeopleDownSyncDownloaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val OUTPUT_DOWN_SYNC = "OUTPUT_DOWN_SYNC"
    }

    override val tag: String = PeopleDownSyncDownloaderWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var peopleDownSyncDownloaderTask: PeopleDownSyncDownloaderTask
    @Inject lateinit var downSyncScopeRepository: PeopleDownSyncScopeRepository

    private val jsonForOp by lazy {
        inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            getComponent<PeopleDownSyncDownloaderWorker> { it.inject(this@PeopleDownSyncDownloaderWorker) }
            val downSyncOperation = extractSubSyncScopeFromInput()
            crashlyticsLog("Start - Params: $downSyncOperation")

            execute(downSyncOperation)
        } catch (t: Throwable) {
            fail(t)
        }
    }

    private suspend fun execute(downSyncOperation: PeopleDownSyncOperation): Result {
        return try {
            val totalDownloaded = peopleDownSyncDownloaderTask.execute(
                downSyncOperation,
                id.toString(),
                this)

            success(workDataOf(OUTPUT_DOWN_SYNC to totalDownloaded), "Total downloaded: $totalDownloaded for $downSyncOperation")
        } catch (t: Throwable) {
            retryOrFailIfCloudIntegrationError(t)
        }
    }

    private fun retryOrFailIfCloudIntegrationError(t: Throwable): Result {
        return if (t is SyncCloudIntegrationException) {
            fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
        } else {
            retry(t)
        }
    }

    private suspend fun extractSubSyncScopeFromInput(): PeopleDownSyncOperation {
        val op = JsonHelper.gson.fromJson(jsonForOp, PeopleDownSyncOperation::class.java)
        return downSyncScopeRepository.refreshDownSyncOperationFromDb(op) ?: op
    }

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_DOWN_SYNC to count)
        )
    }
}

fun WorkInfo.extractDownSyncProgress(peopleSyncCache: PeopleSyncCache): Int? {
    val progress = this.progress.getInt(PROGRESS_DOWN_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_DOWN_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = peopleSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}
