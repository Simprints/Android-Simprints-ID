package com.simprints.id.services.scheduledSync.peopleDownSync.workers.count

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.syncscope.DownSyncScopeRepository
import com.simprints.id.data.db.syncscope.domain.DownSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.SimCoroutineWorker
import timber.log.Timber
import javax.inject.Inject

class CountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val COUNT_PROGRESS = "COUNT_PROGRESS"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var personRepository: PersonRepository
    @Inject lateinit var downSyncScopeRepository: DownSyncScopeRepository

    override suspend fun doWork(): Result {
        getComponent<CountWorker> { it.inject(this) }

        val downSyncScope = downSyncScopeRepository.getDownSyncScope()
        Timber.d("CountWorker - started: $downSyncScope")

        logMessageForCrashReport("Making count request for $downSyncScope")

        return try {
            val counts = execute(downSyncScope)
            logSuccess(downSyncScope)
            Timber.d("CountWorker - done for : $downSyncScope")

            Result.success(workDataOf( COUNT_PROGRESS to JsonHelper.gson.toJson(counts)))
        } catch (t: Throwable) {
            logFailure(t, downSyncScope)

            Timber.d("CountWorker - failed for : $downSyncScope")
            Result.retry()
        }
    }

    private fun logSuccess(syncScope: DownSyncScope) {
        showToastForDebug<CountWorker>(syncScope, Result.success())
    }

    private fun logFailure(t: Throwable, syncScope: DownSyncScope) {
        Timber.e(t)
        showToastForDebug<CountWorker>(syncScope, Result.failure())
        crashReportManager.logExceptionOrSafeException(t)
    }

    private suspend fun execute(syncScope: DownSyncScope) =
        personRepository.countToDownSync(syncScope)

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)
}
