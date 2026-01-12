package com.simprints.infra.eventsync.sync.down.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.local.migration.RealmToRoomMigrationFlagsStore
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.eventsync.sync.common.WorkerProgressCountReporter
import com.simprints.infra.eventsync.sync.down.tasks.BaseEventDownSyncTask
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal abstract class BaseEventDownSyncDownloaderWorker(
    context: Context,
    params: WorkerParameters,
    protected val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val syncCache: EventSyncCache,
    protected val jsonHelper: JsonHelper,
    protected val eventRepository: EventRepository,
    protected val configRepository: ConfigRepository,
    private val dispatcher: CoroutineDispatcher,
    private val realmToRoomMigrationFlagsStore: RealmToRoomMigrationFlagsStore,
) : SimCoroutineWorker(context, params),
    WorkerProgressCountReporter {
    override val tag: String = "EventDownSyncDownloader"

    private val downSyncOperationInput by lazy {
        val jsonInput = inputData.getString(INPUT_DOWN_SYNC_OPS)
            ?: throw IllegalArgumentException("input required")
        jsonHelper.json.decodeFromString<EventDownSyncOperation>(jsonInput)
    }

    private suspend fun getEventScope() = inputData
        .getString(INPUT_EVENT_DOWN_SYNC_SCOPE_ID)
        ?.let { eventRepository.getEventScope(it) }
        ?: throw IllegalArgumentException("input required")

    private suspend fun getDownSyncOperation() = eventDownSyncScopeRepository.refreshState(downSyncOperationInput)

    abstract fun createDownSyncTask(): BaseEventDownSyncTask

    abstract fun handleSyncException(t: Throwable): Result

    override suspend fun doWork(): Result {
        // Check if the migration is in progress before starting the sync
        if (realmToRoomMigrationFlagsStore.isMigrationInProgress()) {
            // this will make the worker retry in 5 minutes
            return Result.retry()
        }
        realmToRoomMigrationFlagsStore.setDownSyncInProgress(true)

        return try {
            performDownSync()
        } finally {
            realmToRoomMigrationFlagsStore.setDownSyncInProgress(false)
        }
    }

    protected open suspend fun performDownSync(): Result = withContext(dispatcher) {
        showProgressNotification()
        crashlyticsLog("Started")
        try {
            val workerId = id.toString()
            var count = syncCache.readProgress(workerId)
            var max: Int? = syncCache.readMax(workerId)
            val project = configRepository.getProject()

            if (project == null) {
                fail(IllegalStateException("User is not signed in"))
            } else {
                createDownSyncTask().downSync(this, getDownSyncOperation(), getEventScope(), project).collect {
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
            }
        } catch (t: Throwable) {
            handleSyncException(t)
        }
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
    val progress = this.progress.getInt(BaseEventDownSyncDownloaderWorker.PROGRESS_DOWN_SYNC, -1)
    val output = this.outputData.getInt(BaseEventDownSyncDownloaderWorker.OUTPUT_DOWN_SYNC, -1)

    // When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readProgress(id.toString())
    return maxOf(progress, output, cached)
}

internal suspend fun WorkInfo.extractDownSyncMaxCount(eventSyncCache: EventSyncCache): Int {
    val progress = this.progress.getInt(BaseEventDownSyncDownloaderWorker.PROGRESS_DOWN_MAX_SYNC, -1)
    val output = this.outputData.getInt(BaseEventDownSyncDownloaderWorker.OUTPUT_DOWN_MAX_SYNC, -1)

    // When the worker is not running (e.g. ENQUEUED due to errors), the output and progress are cleaned.
    val cached = eventSyncCache.readMax(id.toString())
    return maxOf(progress, output, cached)
}
