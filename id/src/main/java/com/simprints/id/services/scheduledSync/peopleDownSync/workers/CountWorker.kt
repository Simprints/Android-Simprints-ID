package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class CountWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val COUNT_WORKER_SCOPE_INPUT = "COUNT_WORKER_SCOPE_INPUT"
        const val COUNT_RESULT = "COUNTS"
    }

    @Inject
    lateinit var crashReportManager: CrashReportManager
    @Inject
    lateinit var syncScopeBuilder: SyncScopesBuilder
    @Inject
    lateinit var countTask: CountTask

    override suspend fun doWork(): Result {
        inject()

        val input = inputData.getString(COUNT_WORKER_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        val syncScope = syncScopeBuilder.fromJsonToSyncScope(input)
            ?: throw IllegalArgumentException("SyncScope required")

        return try {
            logMessageForCrashReport("Making count request for $syncScope")
            val peopleCountsJson = JsonHelper.gson.toJson(countTask.execute(syncScope))

            logToAnalyticsAndToastForDebugBuilds(syncScope, peopleCountsJson)
            Result.success(buildData(peopleCountsJson))

        } catch (e: Throwable) {
            e.printStackTrace()
            logToAnalyticsAndToastForDebugBuilds(syncScope, null)
            crashReportManager.logExceptionOrSafeException(e)
            Result.success()
        }
    }

    private fun buildData(peopleCountsJson: String): Data =
        Data.Builder()
            .putString(COUNT_RESULT, peopleCountsJson)
            .build()

    private fun logToAnalyticsAndToastForDebugBuilds(subSyncScope: SyncScope, jsonOutData: String?) {
        val message = "CountWorker($subSyncScope): Success - $jsonOutData"
        logMessageForCrashReport(message)
        if (BuildConfig.DEBUG) {
            applicationContext.runOnUiThread {
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                Timber.d(message)
            }
        }
    }

    private fun inject() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else throw WorkerInjectionFailedException.forWorker<CountWorker>()
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)
}
