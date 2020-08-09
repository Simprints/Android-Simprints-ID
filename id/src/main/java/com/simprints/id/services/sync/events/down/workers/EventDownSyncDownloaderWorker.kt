package com.simprints.id.services.sync.events.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.exceptions.safe.sync.SyncCloudIntegrationException
import com.simprints.id.exceptions.unexpected.MalformedDownSyncOperationException
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class EventDownSyncDownloaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val OUTPUT_DOWN_SYNC = "OUTPUT_DOWN_SYNC"
    }

    override val tag: String = EventDownSyncDownloaderWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var downSyncHelper: EventDownSyncHelper

    @Inject lateinit var syncCache: EventSyncCache
    @Inject lateinit var jsonHelper: JsonHelper

    internal var eventDownSyncDownloaderTask: EventDownSyncDownloaderTask = EventDownSyncDownloaderTaskImpl()

    private val downSyncOperation by lazy {
        val jsonInput = inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
        jsonHelper.fromJson<EventDownSyncOperation>(jsonInput)
    }

    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        try {
            traceWorkerPerformance()
            getComponent<EventDownSyncDownloaderWorker> { it.inject(this@EventDownSyncDownloaderWorker) }
            Timber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Started")

            crashlyticsLog("Start - Params: $downSyncOperation")

            val count = eventDownSyncDownloaderTask.execute(
                this@EventDownSyncDownloaderWorker.id.toString(),
                downSyncOperation,
                downSyncHelper,
                syncCache,
                this@EventDownSyncDownloaderWorker,
                this)

            Timber.tag(SYNC_LOG_TAG).d("[xDOWNLOADER] Done $count")
            success(workDataOf(OUTPUT_DOWN_SYNC to count), "Total downloaded: $0 for $downSyncOperation")
        } catch (t: Throwable) {
            Timber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Failed")

            retryOrFailIfCloudIntegrationErrorOrMalformedOperation(t)
        }
    }

    private fun retryOrFailIfCloudIntegrationErrorOrMalformedOperation(t: Throwable): Result {
        return if (t is SyncCloudIntegrationException || t is MalformedDownSyncOperationException) {
            fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
        } else {
            retry(t)
        }
    }

//    private suspend fun extractSubSyncScopeFromInput(): EventsDownSyncOperation {
//        try {
//            val op = jsonHelper.fromJson<EventsDownSyncOperation>(jsonForOp)
//            return downSyncScopeRepository.refreshDownSyncOperationFromDb(op) ?: op
//        } catch (t: Throwable) {
//            Timber.d(t)
//            throw MalformedDownSyncOperationException()
//        }
//    }

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_DOWN_SYNC to count)
        )
    }
}

fun WorkInfo.extractDownSyncProgress(eventSyncCache: EventSyncCache): Int? {
    val progress = this.progress.getInt(PROGRESS_DOWN_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_DOWN_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}
