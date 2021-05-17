package com.simprints.id.services.sync.events.up.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.up.EventUpSyncHelper
import com.simprints.id.services.sync.events.up.workers.EventUpSyncCountWorker.Companion.OUTPUT_COUNT_WORKER_UP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class EventUpSyncCountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val INPUT_COUNT_WORKER_UP = "INPUT_COUNT_WORKER_UP"
        const val OUTPUT_COUNT_WORKER_UP = "OUTPUT_COUNT_WORKER_UP"
    }

    override val tag: String = EventUpSyncCountWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var eventUpSyncHelper: EventUpSyncHelper
    @Inject lateinit var jsonHelper: JsonHelper

    private val upSyncScope by lazy {
        val jsonInput = inputData.getString(INPUT_COUNT_WORKER_UP)
            ?: throw IllegalArgumentException("input required")
        Timber.d("Received $jsonInput")
        jsonHelper.fromJson<com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope>(jsonInput)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            getComponent<EventUpSyncCountWorker> { it.inject(this@EventUpSyncCountWorker) }
            Timber.tag(SYNC_LOG_TAG).d("[COUNT_UP] Started")

            crashlyticsLog("Start - $upSyncScope")

            execute(upSyncScope)
        } catch (t: Throwable) {
            Timber.tag(SYNC_LOG_TAG).d("[COUNT_UP] Failed ${t.message}")

            fail(t)
        }
    }

    private suspend fun execute(upSyncScope: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope): Result {
        val upCount = getUpCount(upSyncScope)
        Timber.tag(SYNC_LOG_TAG).d("[COUNT_UP] Done $upCount")

        return success(workDataOf(
            OUTPUT_COUNT_WORKER_UP to upCount), "Total to upload: $upCount")

    }

    private suspend fun getUpCount(upSyncScope: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncScope) =
        eventUpSyncHelper.countForUpSync(upSyncScope.operation)
}

fun WorkInfo.getUpCountsFromOutput(): Int? =
    this.outputData.getInt(OUTPUT_COUNT_WORKER_UP, -1).let {
        if (it > -1) {
            it
        } else {
            null
        }
    }
