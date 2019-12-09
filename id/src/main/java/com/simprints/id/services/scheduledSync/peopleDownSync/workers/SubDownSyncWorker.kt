package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.downsync.DownSyncTask
import timber.log.Timber
import javax.inject.Inject

class SubDownSyncWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT = "SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var scopesBuilder: SyncScopesBuilder
    @Inject lateinit var downSyncTask: DownSyncTask

    override suspend fun doWork(): Result {
        getComponent<CountWorker> { it.inject(this) }

        val subSyncScope = extractSubSyncScopeFromInput(inputData)
        logMessageForCrashReport("Making downsync request for $subSyncScope")

        return try {
            downSyncTask.execute(subSyncScope)
            logSuccess(subSyncScope)
            Result.success()
        } catch (t: Throwable) {
            logFailure(subSyncScope, t)
            Result.retry()
        }
    }

    private fun logFailure(subSyncScope: SubSyncScope, t: Throwable) {
        Timber.e(t)
        crashReportManager.logExceptionOrSafeException(t)
        showToastForDebug<SubDownSyncWorker>(subSyncScope, Result.failure())
    }

    private fun logSuccess(subSyncScope: SubSyncScope) {
        logMessageForCrashReport("DownSyncing for $subSyncScope")
        showToastForDebug<SubDownSyncWorker>(syncScope, Result.success())

    }

    private fun extractSubSyncScopeFromInput(inputData: Data): SubSyncScope {
        val input = inputData.getString(SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        return scopesBuilder.fromJsonToSubSyncScope(input)
            ?: throw IllegalArgumentException("SyncScope required")
    }

    val syncScope = extractSubSyncScopeFromInput(inputData)

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)
}
