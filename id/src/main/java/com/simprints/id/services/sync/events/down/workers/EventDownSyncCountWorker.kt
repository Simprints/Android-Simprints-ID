package com.simprints.id.services.sync.events.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkInfo.State.ENQUEUED
import androidx.work.WorkInfo.State.RUNNING
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.fasterxml.jackson.core.type.TypeReference
import com.simprints.core.exceptions.SyncCloudIntegrationException
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.domain.EventCount
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.common.TAG_MASTER_SYNC_ID
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.workers.EventDownSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_DOWN
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.Companion.tagForType
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.DOWNLOADER
import com.simprints.id.services.sync.events.master.models.EventSyncWorkerType.UPLOADER
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EventDownSyncCountWorker(
    val context: Context,
    params: WorkerParameters,
) : SimCoroutineWorker(context, params) {

    companion object {
        const val INPUT_COUNT_WORKER_DOWN = "INPUT_COUNT_WORKER_DOWN"
        const val OUTPUT_COUNT_WORKER_DOWN = "OUTPUT_COUNT_WORKER_DOWN"
    }

    override val tag: String = EventDownSyncCountWorker::class.java.simpleName

    var wm: WorkManager by lazyVar {
        WorkManager.getInstance(context)
    }

    @Inject lateinit var eventDownSyncHelper: EventDownSyncHelper
    @Inject lateinit var jsonHelper: JsonHelper
    @Inject lateinit var eventDownSyncScopeRepository: com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
    @Inject lateinit var dispatcher: DispatcherProvider

    private val downSyncScope by lazy {
        val jsonInput = inputData.getString(INPUT_COUNT_WORKER_DOWN)
            ?: throw IllegalArgumentException("input required")
        Simber.d("Received $jsonInput")
        jsonHelper.fromJson<com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope>(jsonInput)
    }

    override suspend fun doWork(): Result {
        getComponent<EventDownSyncCountWorker> { it.inject(this@EventDownSyncCountWorker) }

        return withContext(dispatcher.io()) {
            Simber.tag(SYNC_LOG_TAG).d("[COUNT_DOWN] Started")
            try {
                crashlyticsLog("Start - Params: $downSyncScope")
                execute(downSyncScope)
            } catch (t: Throwable) {
                fail(t)
            }
        }
    }

    private suspend fun execute(downSyncScope: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope): Result {
        return try {

            val downCount = getDownCount(downSyncScope)
            val output = jsonHelper.toJson(downCount)

            Simber.tag(SYNC_LOG_TAG).d("[COUNT_DOWN] Done $downCount")
            success(workDataOf(OUTPUT_COUNT_WORKER_DOWN to output), output)

        } catch (t: Throwable) {
            Simber.tag(SYNC_LOG_TAG).d("[COUNT_DOWN] Failed. ${t.message}")

            when {
                t is SyncCloudIntegrationException -> {
                    fail(t)
                }
                isSyncStillRunning() -> {
                    retry(t)
                }
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

    private suspend fun getDownCount(downSyncScope: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope) =
        downSyncScope.operations.map {
            val opWithLastState = eventDownSyncScopeRepository.refreshState(it)
            eventDownSyncHelper.countForDownSync(opWithLastState)
        }.flatten()

}

fun WorkInfo.getDownCountsFromOutput(): List<EventCount>? {
    val outputJson = this.outputData.getString(OUTPUT_COUNT_WORKER_DOWN)
    return try {
        JsonHelper.fromJson(outputJson!!, object : TypeReference<List<EventCount>>() {})
    } catch (t: Throwable) {
        null
    }
}

