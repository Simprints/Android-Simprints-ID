package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTask
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class SubDownSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT = "SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var scopesBuilder: SyncScopesBuilder
    @Inject lateinit var downSyncTask: DownSyncTask

    override fun doWork(): Result {
        inject()

        val input = inputData.getString(SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        val subSyncScope = scopesBuilder.fromJsonToSubSyncScope(input)
            ?: throw IllegalArgumentException("SyncScope required")

        val result = try {
            logMessageForCrashReport("DownSyncing for $subSyncScope")
            downSyncTask.execute(subSyncScope).blockingAwait()
            Result.success()
        } catch (e: Throwable) {
            e.printStackTrace()
            crashReportManager.logExceptionOrThrowable(e)
            Result.failure()
        }

        logToAnalyticsAndToastForDebugBuilds(subSyncScope, result)
        return result
    }

    private fun logToAnalyticsAndToastForDebugBuilds(subSyncScope: SubSyncScope, result: Result) {
        val message = "WM - SubDownSyncWorker($subSyncScope): $result"
        logMessageForCrashReport(message)
        if (BuildConfig.DEBUG) {
            applicationContext.runOnUiThread {
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                Timber.d(message)
            }
        }
    }

    private fun inject() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else throw WorkerInjectionFailedException.forWorker<SubDownSyncWorker>()
    }

    private fun logMessageForCrashReport(message: String) =
        crashReportManager.logMessageForCrashReport(CrashReportTag.SYNC, CrashReportTrigger.NETWORK, message = message)
}
