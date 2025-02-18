package com.simprints.infra.eventsync.sync.down.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.event.remote.exceptions.TooManyRequestsException
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS
import com.simprints.infra.eventsync.sync.common.WorkerProgressCountReporter
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncTask
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_MAX_SYNC
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_MAX_SYNC
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
internal class EventDownSyncDownloaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val downSyncTask: EventDownSyncTask,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val syncCache: EventSyncCache,
    private val jsonHelper: JsonHelper,
    private val eventRepository: EventRepository,
    private val configRepository: ConfigRepository,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params),
    WorkerProgressCountReporter {
    override val tag: String = "EventDownSyncDownloader"

    private val downSyncOperationInput by lazy {
        val jsonInput = inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
        jsonHelper.fromJson<EventDownSyncOperation>(
            jsonInput,
        )
    }

    private suspend fun getEventScope() = inputData
        .getString(INPUT_EVENT_DOWN_SYNC_SCOPE_ID)
        ?.let { eventRepository.getEventScope(it) }
        ?: throw IllegalArgumentException("input required")

    private suspend fun getDownSyncOperation() = eventDownSyncScopeRepository.refreshState(downSyncOperationInput)

    override suspend fun doWork(): Result = withContext(dispatcher) {
        crashlyticsLog("Started")
        showProgressNotification()
        try {
            val workerId = id.toString()
            var count = syncCache.readProgress(workerId)
            var max: Int? = syncCache.readMax(workerId)
            val project = configRepository.getProject()

            downSyncTask.downSync(this, getDownSyncOperation(), getEventScope(), project).collect {
                count = it.progress
                max = it.maxProgress
                syncCache.saveProgress(workerId, count)
                syncCache.saveMax(workerId, max)
                reportCount(count, max)
            }

            Simber.d("Downloaded events: $count", tag = tag)
            success(
                workDataOf(
                    OUTPUT_DOWN_SYNC to count,
                    OUTPUT_DOWN_MAX_SYNC to max,
                ),
                "Total downloaded: $count / $max",
            )
        } catch (t: Throwable) {
            handleSyncException(t)
        }
    }

    private fun handleSyncException(t: Throwable) = when (t) {
        is IllegalArgumentException -> fail(t, t.message)

        is BackendMaintenanceException -> fail(
            t,
            t.message,
            workDataOf(
                OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                OUTPUT_ESTIMATED_MAINTENANCE_TIME to t.estimatedOutage,
            ),
        )

        is SyncCloudIntegrationException -> fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
        is TooManyRequestsException -> fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_TOO_MANY_REQUESTS to true))
        is RemoteDbNotSignedInException -> fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED to true))
        else -> retry(t)
    }

    override suspend fun reportCount(
        count: Int,
        maxCount: Int?,
    ) {
        setProgress(
            workDataOf(
                PROGRESS_DOWN_SYNC to count,
                PROGRESS_DOWN_MAX_SYNC to maxCount,
            ),
        )
    }

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val INPUT_EVENT_DOWN_SYNC_SCOPE_ID = "INPUT_EVENT_DOWN_SYNC_SCOPE_ID"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val PROGRESS_DOWN_MAX_SYNC = "PROGRESS_DOWN_MAX_SYNC"
        const val OUTPUT_DOWN_SYNC = "OUTPUT_DOWN_SYNC"
        const val OUTPUT_DOWN_MAX_SYNC = "OUTPUT_DOWN_MAX_SYNC"
    }
}

internal suspend fun WorkInfo.extractDownSyncProgress(eventSyncCache: EventSyncCache): Int {
    val progress = this.progress.getInt(PROGRESS_DOWN_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_DOWN_SYNC, -1)

    // When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}

internal suspend fun WorkInfo.extractDownSyncMaxCount(eventSyncCache: EventSyncCache): Int {
    val progress = this.progress.getInt(PROGRESS_DOWN_MAX_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_DOWN_MAX_SYNC, -1)

    // When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readMax(id.toString())
    return maxOf(progress, output, cached)
}
