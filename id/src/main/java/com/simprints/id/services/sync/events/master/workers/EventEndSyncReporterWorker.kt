package com.simprints.id.services.sync.events.master.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.services.sync.events.common.SimCoroutineWorker
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

/**
 * It's executed at the end of the sync, when all workers succeed (downloaders and uploaders).
 * It stores the "last successful timestamp"
 */
class EventEndSyncReporterWorker(appContext: Context,
                                 params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    override val tag: String = EventEndSyncReporterWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var syncCache: EventSyncCache

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                getComponent<EventSyncMasterWorker> { it.inject(this@EventEndSyncReporterWorker) }
                val syncId = inputData.getString(SYNC_ID_TO_MARK_AS_COMPLETED)
                crashlyticsLog("Start - Params: $syncId")

                if (!syncId.isNullOrEmpty()) {
                    syncCache.storeLastSuccessfulSyncTime(Date())
                    success()
                } else {
                    throw IllegalArgumentException("SyncId missed")
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                fail(t)
            }
        }

    companion object {
        const val SYNC_ID_TO_MARK_AS_COMPLETED = "SYNC_ID_TO_MARK_AS_COMPLETED"
    }
}
