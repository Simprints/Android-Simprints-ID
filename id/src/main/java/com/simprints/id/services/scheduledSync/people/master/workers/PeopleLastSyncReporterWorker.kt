package com.simprints.id.services.scheduledSync.people.master.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import java.util.*
import javax.inject.Inject

class PeopleLastSyncReporterWorker(appContext: Context,
                                   params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    override val tag: String = PeopleLastSyncReporterWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var syncCache: PeopleSyncCache

    override suspend fun doWork(): Result {
        getComponent<PeopleSyncMasterWorker> { it.inject(this) }
        val syncId = inputData.getString(SYNC_ID_TO_MARK_AS_COMPLETED)
        crashlyticsLog("Start - Params: $syncId")

        syncId?.let {
            syncCache.lastSuccessfulSyncTime = Date()
        }

        return success()
    }

    companion object {
        const val SYNC_ID_TO_MARK_AS_COMPLETED = "SYNC_ID_TO_MARK_AS_COMPLETED"
    }
}
