package com.simprints.infra.eventsync.sync.up.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.DispatcherBG
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.workers.SimCoroutineWorker
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope
import com.simprints.infra.eventsync.sync.common.SYNC_LOG_TAG
import com.simprints.infra.eventsync.sync.up.workers.EventUpSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_UP
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@HiltWorker
internal class EventUpSyncCountWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val eventRepository: EventRepository,
    private val jsonHelper: JsonHelper,
    @DispatcherBG private val dispatcher: CoroutineDispatcher,
) : SimCoroutineWorker(context, params) {

    companion object {
        const val INPUT_COUNT_WORKER_UP = "INPUT_COUNT_WORKER_UP"
        const val OUTPUT_COUNT_WORKER_UP = "OUTPUT_COUNT_WORKER_UP"
    }

    override val tag: String = EventUpSyncCountWorker::class.java.simpleName

    private val upSyncScope by lazy {
        val jsonInput = inputData.getString(INPUT_COUNT_WORKER_UP)
            ?: throw IllegalArgumentException("input required")
        Simber.d("Received $jsonInput")
        jsonHelper.fromJson<EventUpSyncScope>(
            jsonInput
        )
    }

    override suspend fun doWork(): Result = withContext(dispatcher) {
        try {
            Simber.tag(SYNC_LOG_TAG).d("[COUNT_UP] Started")
            crashlyticsLog("Start - $upSyncScope")

            val upCount = getUpCount(upSyncScope)

            Simber.tag(SYNC_LOG_TAG).d("[COUNT_UP] Done $upCount")
            success(
                workDataOf(OUTPUT_COUNT_WORKER_UP to upCount),
                "Total to upload: $upCount"
            )
        } catch (t: Throwable) {
            Simber.tag(SYNC_LOG_TAG).d("[COUNT_UP] Failed ${t.message}")
            fail(t)
        }
    }

    private suspend fun getUpCount(upSyncScope: EventUpSyncScope) = eventRepository
        .observeEventCount(upSyncScope.operation.projectId, null)
        .firstOrNull() ?: 0
}

internal fun WorkInfo.getUpCountsFromOutput(): Int? =
    this.outputData.getInt(OUTPUT_COUNT_WORKER_UP, -1).let {
        if (it > -1) {
            it
        } else {
            null
        }
    }
