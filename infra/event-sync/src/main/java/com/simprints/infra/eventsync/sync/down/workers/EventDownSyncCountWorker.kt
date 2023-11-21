package com.simprints.infra.eventsync.sync.down.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.infra.eventsync.status.models.EventSyncWorkerType.UPLOADER
import com.simprints.infra.eventsync.sync.common.OUTPUT_ESTIMATED_MAINTENANCE_TIME
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE
import com.simprints.infra.eventsync.sync.common.OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION
import com.simprints.infra.eventsync.sync.common.SYNC_LOG_TAG
import com.simprints.infra.eventsync.sync.common.TAG_MASTER_SYNC_ID
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncCountTask
import com.simprints.infra.eventsync.sync.down.workers.EventDownSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_DOWN
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@HiltWorker
internal class EventDownSyncCountWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val jsonHelper: JsonHelper,
    private val eventDownSyncCountTask: EventDownSyncCountTask,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {

    companion object {
        const val INPUT_COUNT_WORKER_DOWN = "INPUT_COUNT_WORKER_DOWN"
        const val OUTPUT_COUNT_WORKER_DOWN = "OUTPUT_COUNT_WORKER_DOWN"
    }

    private val wm = WorkManager.getInstance(context)

    override val tag: String = EventDownSyncCountWorker::class.java.simpleName

    private val downSyncScope by lazy {
        val jsonInput = inputData.getString(INPUT_COUNT_WORKER_DOWN)
            ?: throw IllegalArgumentException("input required")
        Simber.d("Received $jsonInput")
        jsonHelper.fromJson<EventDownSyncScope>(jsonInput)
    }

    override suspend fun doWork(): Result = withContext(dispatcher) {
        Simber.tag(SYNC_LOG_TAG).d("[COUNT_DOWN] Started")
        try {
            crashlyticsLog("Start - Params: $downSyncScope")

            val downCount = eventDownSyncCountTask.getCount(downSyncScope)
            val output = jsonHelper.toJson(downCount)

            Simber.tag(SYNC_LOG_TAG).d("[COUNT_DOWN] Done $downCount")
            success(workDataOf(OUTPUT_COUNT_WORKER_DOWN to output), output)

        } catch (t: Throwable) {
            Simber.tag(SYNC_LOG_TAG).d("[COUNT_DOWN] Failed. ${t.message}")

            when {
                t is SyncCloudIntegrationException -> {
                    fail(t, t.message, workDataOf(OUTPUT_FAILED_BECAUSE_CLOUD_INTEGRATION to true))
                }
                t is BackendMaintenanceException -> fail(
                    t, t.message,
                    workDataOf(
                        OUTPUT_FAILED_BECAUSE_BACKEND_MAINTENANCE to true,
                        OUTPUT_ESTIMATED_MAINTENANCE_TIME to t.estimatedOutage
                    )
                )
                isSyncStillRunning() -> retry(t)
                else -> {
                    Simber.d(t)
                    t.printStackTrace()
                    success(message = "Succeed because count is not required any more.")
                }
            }
        }
    }

    private fun isSyncStillRunning(): Boolean {
        val masterSyncIdTag = this.tags.firstOrNull { it.contains(TAG_MASTER_SYNC_ID) }
            ?: return false

        val workers = wm.getWorkInfosByTag(masterSyncIdTag).get()
        return workers?.let {
            val downloaders = it.filter { it.tags.contains(tagForType(DOWNLOADER)) }
            val uploaders = it.filter { it.tags.contains(tagForType(UPLOADER)) }
            (downloaders + uploaders).any {
                listOf(RUNNING, ENQUEUED).contains(it.state)
            }
        } ?: false
    }
}

internal fun WorkInfo.getDownCountsFromOutput(): List<EventCount>? {
    val outputJson = this.outputData.getString(OUTPUT_COUNT_WORKER_DOWN)
    return try {
        JsonHelper.fromJson(outputJson!!, object : TypeReference<List<EventCount>>() {})
    } catch (t: Throwable) {
        null
    }
}

