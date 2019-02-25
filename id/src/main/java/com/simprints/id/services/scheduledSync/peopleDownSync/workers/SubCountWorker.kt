package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.crashReport.CrashReportManager
import com.simprints.id.data.analytics.crashReport.CrashReportTags
import com.simprints.id.data.analytics.crashReport.CrashReportTrigger
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class SubCountWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val SUBCOUNT_WORKER_SUB_SCOPE_INPUT = "SUBCOUNT_WORKER_SUB_SCOPE_INPUT"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder
    @Inject lateinit var countTask: CountTask

    override fun doWork(): Result {
        inject()

        val input = inputData.getString(SUBCOUNT_WORKER_SUB_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        val subSyncScope = syncScopeBuilder.fromJsonToSubSyncScope(input)
            ?: throw IllegalArgumentException("SyncScope required")
        val key = subSyncScope.uniqueKey

        return try {
            logMessageForCrashReport("Making count request for $subSyncScope")
            val totalCount = countTask.execute(subSyncScope).blockingGet()
            val data = Data.Builder()
                .putInt(key, totalCount.toInt())
                .build()

            logToAnalyticsAndToastForDebugBuilds(subSyncScope, data)
            Result.success(data)
        } catch (e: Throwable) {
            e.printStackTrace()
            logToAnalyticsAndToastForDebugBuilds(subSyncScope)
            crashReportManager.logExceptionOrThrowable(e)
            Result.success()
        }
    }

    private fun logToAnalyticsAndToastForDebugBuilds(subSyncScope: SubSyncScope, data: Data? = null) {
        val message = "SubCountWorker($subSyncScope): Success - ${data?.keyValueMap}"
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
        } else throw WorkerInjectionFailedException.forWorker<SubCountWorker>()
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTags.SYNC, CrashReportTrigger.NETWORK, message = message)
}
