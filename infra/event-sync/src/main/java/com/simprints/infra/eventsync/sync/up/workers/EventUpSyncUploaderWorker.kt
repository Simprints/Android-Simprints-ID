package com.simprints.infra.eventsync.sync.up.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.exceptions.MalformedSyncOperationException
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED
import com.simprints.infra.eventsync.sync.common.SYNC_LOG_TAG
import com.simprints.infra.eventsync.sync.common.WorkerProgressCountReporter
import com.simprints.infra.eventsync.sync.up.tasks.EventUpSyncTask
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.OUTPUT_UP_MAX_SYNC
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.OUTPUT_UP_SYNC
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.PROGRESS_UP_MAX_SYNC
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncUploaderWorker.Companion.PROGRESS_UP_SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@HiltWorker
internal class EventUpSyncUploaderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val upSyncTask: EventUpSyncTask,
    private val eventSyncCache: EventSyncCache,
    private val authStore: AuthStore,
    private val jsonHelper: JsonHelper,
    private val eventRepository: EventRepository,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    override val tag: String = EventUpSyncUploaderWorker::class.java.simpleName

    private val upSyncScope by lazy {
        try {
            val jsonInput = inputData.getString(INPUT_UP_SYNC)
                ?: throw IllegalArgumentException("input required")
            Simber.d("Received $jsonInput")

            jsonHelper.fromJson(jsonInput)
        } catch (t: Throwable) {
            if (t is JsonParseException || t is JsonMappingException) {
                EventUpSyncScope.ProjectScope(authStore.signedInProjectId)
            } else {
                throw MalformedSyncOperationException(t.message ?: "")
            }
        }
    }

    private suspend fun getEventScope() = inputData
        .getString(INPUT_EVENT_UP_SYNC_SCOPE_ID)
        ?.let { eventRepository.getEventScope(it) }
        ?: throw IllegalArgumentException("input required")


    override suspend fun doWork(): Result = withContext(dispatcher) {
        try {
            Simber.tag(SYNC_LOG_TAG).d("[UPLOADER] Started")
            showProgressNotification()

            val workerId = this@EventUpSyncUploaderWorker.id.toString()
            var count = eventSyncCache.readProgress(workerId)
            val max = eventRepository
                .observeEventCount(null)
                .firstOrNull() ?: 0

            crashlyticsLog("Start")
            upSyncTask.upSync(upSyncScope.operation, getEventScope()).collect {
                count += it.progress
                eventSyncCache.saveProgress(workerId, count)
                Simber.tag(SYNC_LOG_TAG).d("[UPLOADER] Uploaded $count for batch : $it")

                reportCount(count, max)
            }

            Simber.tag(SYNC_LOG_TAG).d("[UPLOADER] Done")
            success(
                workDataOf(
                    OUTPUT_UP_SYNC to count,
                    OUTPUT_UP_MAX_SYNC to max,
                ),
                "Total uploaded: $count / $max"
            )
        } catch (t: Throwable) {
            Simber.d(t)
            Simber.tag(SYNC_LOG_TAG).d("[UPLOADER] Failed ${t.message}")
            retryOrFailIfCloudIntegrationOrBackendMaintenanceError(t)
        }
    }

    private fun retryOrFailIfCloudIntegrationOrBackendMaintenanceError(t: Throwable) = when (t) {
        is IllegalArgumentException -> fail(t, t.message)
        is BackendMaintenanceException -> {
            fail(
                t,
                t.message,
                workDataOf(
                    OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                    OUTPUT_ESTIMATED_MAINTENANCE_TIME to t.estimatedOutage
                )
            )
        }
        is RemoteDbNotSignedInException -> {
            fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_RELOGIN_REQUIRED to true))
        }

        is SyncCloudIntegrationException -> {
            fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
        }

        else -> {
            retry(t)
        }
    }

    override suspend fun reportCount(count: Int, maxCount: Int?) {
        setProgress(
            workDataOf(
                PROGRESS_UP_SYNC to count,
                PROGRESS_UP_MAX_SYNC to maxCount,
            )
        )
    }

    companion object {

        const val INPUT_UP_SYNC = "INPUT_UP_SYNC"
        const val INPUT_EVENT_UP_SYNC_SCOPE_ID = "INPUT_EVENT_UP_SYNC_SCOPE_ID"
        const val PROGRESS_UP_SYNC = "PROGRESS_UP_SYNC"
        const val PROGRESS_UP_MAX_SYNC = "PROGRESS_UP_MAX_SYNC"
        const val OUTPUT_UP_SYNC = "OUTPUT_UP_SYNC"
        const val OUTPUT_UP_MAX_SYNC = "OUTPUT_UP_MAX_SYNC"
    }
}

internal suspend fun WorkInfo.extractUpSyncProgress(eventSyncCache: EventSyncCache): Int {
    val progress = this.progress.getInt(PROGRESS_UP_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_UP_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}

internal suspend fun WorkInfo.extractUpSyncMaxCount(eventSyncCache: EventSyncCache): Int {
    val progress = this.progress.getInt(PROGRESS_UP_MAX_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_UP_MAX_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readMax(id.toString())
    return maxOf(progress, output, cached)
}
