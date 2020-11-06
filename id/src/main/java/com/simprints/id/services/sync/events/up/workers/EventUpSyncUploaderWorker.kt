package com.simprints.id.services.sync.events.up.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncScope
import com.simprints.id.exceptions.unexpected.MalformedDownSyncOperationException
import com.simprints.id.exceptions.unexpected.SyncCloudIntegrationException
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.services.sync.events.master.internal.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.id.services.sync.events.up.EventUpSyncHelper
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.id.services.sync.events.up.workers.EventUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

// TODO: uncomment userId when multitenancy is properly implemented
@InternalCoroutinesApi
class EventUpSyncUploaderWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    override val tag: String = EventUpSyncUploaderWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var upSyncHelper: EventUpSyncHelper
    @Inject lateinit var eventSyncCache: EventSyncCache
    @Inject lateinit var jsonHelper: JsonHelper

    private val upSyncScope by lazy {
        try {
            val jsonInput = inputData.getString(INPUT_UP_SYNC)
                ?: throw IllegalArgumentException("input required")
            Timber.d("Received $jsonInput")
            jsonHelper.fromJson<EventUpSyncScope>(jsonInput)
        } catch (t: Throwable) {
            throw MalformedDownSyncOperationException(t.message ?: "")
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            getComponent<EventUpSyncUploaderWorker> { it.inject(this@EventUpSyncUploaderWorker) }
            Timber.tag(SYNC_LOG_TAG).d("[UPLOADER] Started")

            val workerId = this@EventUpSyncUploaderWorker.id.toString()
            var count = eventSyncCache.readProgress(workerId)

            crashlyticsLog("Start")
            val totalUploaded = upSyncHelper.upSync(this, upSyncScope.operation)
            while (!totalUploaded.isClosedForReceive) {
                totalUploaded.poll()?.let {
                    count += it.progress
                    eventSyncCache.saveProgress(workerId, count)
                    Timber.tag(SYNC_LOG_TAG).d("[UPLOADER] Uploaded $count for batch : $it")

                    reportCount(count)
                }
            }

            Timber.tag(SYNC_LOG_TAG).d("[UPLOADER] Done")
            success(workDataOf(OUTPUT_UP_SYNC to count), "Total uploaded: $count")
        } catch (t: Throwable) {
            Timber.d(t)
            Timber.tag(SYNC_LOG_TAG).d("[UPLOADER] Failed ${t.message}")
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

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_UP_SYNC to count)
        )
    }

    companion object {
        const val INPUT_UP_SYNC = "INPUT_UP_SYNC"
        const val PROGRESS_UP_SYNC = "PROGRESS_UP_SYNC"
        const val OUTPUT_UP_SYNC = "OUTPUT_UP_SYNC"
    }
}

fun WorkInfo.extractUpSyncProgress(eventSyncCache: EventSyncCache): Int? {
    val progress = this.progress.getInt(PROGRESS_UP_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_UP_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}
