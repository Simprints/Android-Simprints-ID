package com.simprints.id.services.sync.events.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.json.JsonHelper
import com.simprints.eventsystem.event.remote.exceptions.TooManyRequestsException
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.common.TAG_DOWN_SYNC_NEW_MODULES
import com.simprints.id.services.sync.events.common.WorkerProgressCountReporter
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.OUTPUT_DOWN_SYNC
import com.simprints.id.services.sync.events.down.workers.EventDownSyncDownloaderWorker.Companion.PROGRESS_DOWN_SYNC
import com.simprints.id.services.sync.events.master.internal.*
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.BackendMaintenanceException
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EventDownSyncDownloaderWorker(
    context: Context,
    params: WorkerParameters
) : SimCoroutineWorker(context, params), WorkerProgressCountReporter {

    companion object {
        const val INPUT_DOWN_SYNC_OPS = "INPUT_DOWN_SYNC_OPS"
        const val PROGRESS_DOWN_SYNC = "PROGRESS_DOWN_SYNC"
        const val OUTPUT_DOWN_SYNC = "OUTPUT_DOWN_SYNC"
    }

    override val tag: String = EventDownSyncDownloaderWorker::class.java.simpleName

    @Inject
    lateinit var downSyncHelper: EventDownSyncHelper

    @Inject
    lateinit var eventDownSyncScopeRepository: com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository

    @Inject
    lateinit var preferencesManager: IdPreferencesManager

    @Inject
    lateinit var syncCache: EventSyncCache

    @Inject
    lateinit var jsonHelper: JsonHelper

    @Inject
    lateinit var dispatcher: DispatcherProvider

    internal var eventDownSyncDownloaderTask: EventDownSyncDownloaderTask =
        EventDownSyncDownloaderTaskImpl()

    private val downSyncOperationInput by lazy {
        val jsonInput = inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
        jsonHelper.fromJson<com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation>(
            jsonInput
        )
    }

    private suspend fun getDownSyncOperation() =
        eventDownSyncScopeRepository.refreshState(downSyncOperationInput)

    override suspend fun doWork(): Result {
        getComponent<EventDownSyncDownloaderWorker> { it.inject(this@EventDownSyncDownloaderWorker) }

        return try {
            withContext(dispatcher.io()) {
                Simber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Started")

                crashlyticsLog("Start - Params: $downSyncOperationInput")

                //Do not refresh operation if request is for new modules so we get all events,
                //not just the ones after last sync
                val operation = if (isNewModulesDownSyncRequest()) {
                    downSyncOperationInput
                } else {
                    getDownSyncOperation()
                }

                val count = eventDownSyncDownloaderTask.execute(
                    this@EventDownSyncDownloaderWorker.id.toString(),
                    operation,
                    downSyncHelper,
                    syncCache,
                    this@EventDownSyncDownloaderWorker,
                    this
                )

                if (isNewModulesDownSyncRequest()) {
                    preferencesManager.newlyAddedModules = setOf()
                }

                Simber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Done $count")
                success(
                    workDataOf(OUTPUT_DOWN_SYNC to count),
                    "Total downloaded: $0 for $downSyncOperationInput"
                )
            }
        } catch (t: Throwable) {
            Simber.tag(SYNC_LOG_TAG).d("[DOWNLOADER] Failed")
            retryOrFailIfCloudIntegrationErrorOrMalformedOperationOrBackendMaintenance(t)
        }
    }

    private fun isNewModulesDownSyncRequest() = tags.contains(TAG_DOWN_SYNC_NEW_MODULES)

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

fun WorkInfo.extractDownSyncProgress(eventSyncCache: EventSyncCache): Int {
    val progress = this.progress.getInt(PROGRESS_DOWN_SYNC, -1)
    val output = this.outputData.getInt(OUTPUT_DOWN_SYNC, -1)

    //When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}
