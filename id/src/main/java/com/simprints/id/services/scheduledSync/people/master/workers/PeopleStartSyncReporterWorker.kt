package com.simprints.id.services.scheduledSync.people.master.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import javax.inject.Inject

/**
 * It's executed at the beginning of a sync and it sets in the output the unique sync id.
 * The PeopleSyncMasterWorker creates the unique id, but it can't set it as output because
 * it's a periodic worker.
 * The periodic workers transits immediately from SUCCESS to ENQUEUED for the next round.
 * When it's in ENQUEUED the outputData is erased, so we can't extract the uniqueId observing
 * PeopleStartSyncReporterWorker.
 */
class PeopleStartSyncReporterWorker(appContext: Context,
                                    params: WorkerParameters) : SimCoroutineWorker(appContext, params) {

    override val tag: String = PeopleStartSyncReporterWorker::class.java.simpleName

    @Inject override lateinit var crashReportManager: CrashReportManager

    override suspend fun doWork(): Result =
        try {
            getComponent<PeopleSyncMasterWorker> { it.inject(this) }
            val syncId = inputData.getString(SYNC_ID_STARTED)
            crashlyticsLog("Start - Params: $syncId")
            success(inputData)
        } catch (t: Throwable) {
            t.printStackTrace()
            fail(t)
        }

    companion object {
        const val SYNC_ID_STARTED = "SYNC_ID_STARTED"
    }
}
