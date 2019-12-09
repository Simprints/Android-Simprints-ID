package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedException
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.SaveCountsTask
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class CountWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val COUNT_WORKER_SCOPE_INPUT = "COUNT_WORKER_SCOPE_INPUT"
    }

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder
    @Inject lateinit var countTask: CountTask
    @Inject lateinit var saveCountsTask: SaveCountsTask

    override fun doWork(): Result {
        inject()

        val input = inputData.getString(COUNT_WORKER_SCOPE_INPUT)
            ?: throw IllegalArgumentException("input required")
        val syncScope = syncScopeBuilder.fromJsonToSyncScope(input)
            ?: throw IllegalArgumentException("SyncScope required")
        val key = syncScope.uniqueKey

        return try {
            logMessageForCrashReport("Making count request for $syncScope")
            val peopleCounts = getPeopleCountAndSaveInLocal(syncScope)

            val data = Data.Builder()
                .putInt(key, getTotalCount(peopleCounts))
                .build()

            logToAnalyticsAndToastForDebugBuilds(syncScope, data)
            Result.success(data)
        } catch (e: Throwable) {
            e.printStackTrace()
            logToAnalyticsAndToastForDebugBuilds(syncScope)
            crashReportManager.logExceptionOrSafeException(e)
            Result.success()
        }
    }

    private fun getPeopleCountAndSaveInLocal(syncScope: SyncScope)=
        countTask.execute(syncScope).blockingGet().also {
            saveCountsTask.execute(prepareInputForTask(it))
        }

    private fun getTotalCount(peopleCounts: List<PeopleCount>) = peopleCounts.sumBy { it.downloadCount }

    private fun prepareInputForTask(peopleCounts: List<PeopleCount>): Map<SubSyncScope, Int> {
        val inputForTask = mutableMapOf<SubSyncScope, Int>()
        val scope = syncScopeBuilder.buildSyncScope()
        scope?.toSubSyncScopes()?.forEachIndexed { index, subSyncScope ->
            val counter = peopleCounts[index].downloadCount
            counter.let { counterForSubSync ->
                inputForTask[subSyncScope] = counterForSubSync
            }
        }
        return inputForTask
    }

    private fun logToAnalyticsAndToastForDebugBuilds(subSyncScope: SyncScope, data: Data? = null) {
        val message = "CountWorker($subSyncScope): Success - ${data?.keyValueMap}"
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
