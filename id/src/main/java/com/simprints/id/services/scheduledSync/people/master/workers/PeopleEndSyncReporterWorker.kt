package com.simprints.id.services.scheduledSync.people.master.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.data.db.session.crashreport.CrashReportManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

/**
 * It's executed at the end of the sync, when all workers succeed (downloaders and uploaders).
 * It stores the "last successful timestamp"
 */
class PeopleEndSyncReporterWorker(appContext: Context,
                                  params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    override val tag: String = PeopleEndSyncReporterWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var syncCache: PeopleSyncCache

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                getComponent<PeopleSyncMasterWorker> { it.inject(this@PeopleEndSyncReporterWorker) }
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
