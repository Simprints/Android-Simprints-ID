package com.simprints.id.services.scheduledSync.people.master.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.services.scheduledSync.people.common.SimCoroutineWorker
import javax.inject.Inject

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
