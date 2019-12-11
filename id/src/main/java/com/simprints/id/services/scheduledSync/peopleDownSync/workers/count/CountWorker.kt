package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.data.db.syncscope.domain.DownSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.RESULT
import timber.log.Timber
import javax.inject.Inject

class CountWorker(context: Context, params: WorkerParameters) : SimCoroutineWorker(context, params) {

    companion object {
        const val COUNT_WORKER_SCOPE_INPUT = "COUNT_WORKER_SCOPE_INPUT"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder
    @Inject lateinit var personRepository: PersonRepository

    override suspend fun doWork(): Result {
        getComponent<CountWorker> { it.inject(this) }

        val syncScope = extractSyncScopeFromInput(inputData)
        logMessageForCrashReport("Making count request for $syncScope")

        return try {
            val counts = execute(syncScope)
            logSuccess(syncScope)

            Result.success(workDataOf(RESULT to JsonHelper.gson.toJson(counts)))
        } catch (t: Throwable) {
            logFailure(t, syncScope)
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

    private fun extractSyncScopeFromInput(inputData: Data): DownSyncScope {
        val syncScopeJson = inputData.getString(COUNT_WORKER_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        return syncScopeBuilder.fromJsonToSyncScope(syncScopeJson)
            ?: throw IllegalArgumentException("SyncScope required")
    }

    private suspend fun execute(syncScope: DownSyncScope) =
        personRepository.countToDownSync(syncScope)

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)
}
