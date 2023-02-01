package com.simprints.id.services.sync.events.down.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.remote.exceptions.TooManyRequestsException
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.events.master.internal.*
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
class EventDownSyncDownloaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val downSyncHelper: EventDownSyncHelper,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val syncCache: EventSyncCache,
    private val eventDownSyncDownloaderTask: EventDownSyncDownloaderTask,
    private val jsonHelper: JsonHelper,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val OUTPUT_DOWN_SYNC = "OUTPUT_DOWN_SYNC"
    }

    override val tag: String = EventDownSyncDownloaderWorker::class.java.simpleName

    private val downSyncOperationInput by lazy {
        val jsonInput = inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
        jsonHelper.fromJson<EventDownSyncOperation>(
            jsonInput
        )
    }

    private suspend fun getDownSyncOperation() =
        eventDownSyncScopeRepository.refreshState(downSyncOperationInput)

    override suspend fun doWork(): Result =
        withContext(dispatcher) {
            try {
                Simber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Started")

                crashlyticsLog("Start - Params: $downSyncOperationInput")

                val count = eventDownSyncDownloaderTask.execute(
                    this@EventDownSyncDownloaderWorker.id.toString(),
                    getDownSyncOperation(),
                    downSyncHelper,
                    syncCache,
                    this@EventDownSyncDownloaderWorker,
                    this
                )

                Simber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Done $count")
                success(
                    workDataOf(OUTPUT_DOWN_SYNC to count),
                    "Total downloaded: $0 for $downSyncOperationInput"
                )
            } catch (t: Throwable) {
                Simber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Failed")
                retryOrFailIfCloudIntegrationErrorOrMalformedOperationOrBackendMaintenance(t)
            }
        }

    private fun retryOrFailIfCloudIntegrationErrorOrMalformedOperationOrBackendMaintenance(t: Throwable): Result {
        return when (t) {
            is BackendMaintenanceException -> fail(
                t,
                t.message,
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to t.estimatedOutage
                )
            )
            is SyncCloudIntegrationException -> fail(
                t,
                t.message,
                workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true)
            )
            is TooManyRequestsException -> fail(
                t,
                t.message,
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS to true
                )
            )
            else -> retry(t)
        }
    }

    override suspend fun reportCount(count: Int) {
        setProgress(
            workDataOf(PROGRESS_DOWN_SYNC to count)
        )
    }
}

suspend fun WorkInfo.extractDownSyncProgress(eventSyncCache: EventSyncCache): Int {
    val progress = this.progress.getInt(PROGRESS_DOWN_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_DOWN_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}
