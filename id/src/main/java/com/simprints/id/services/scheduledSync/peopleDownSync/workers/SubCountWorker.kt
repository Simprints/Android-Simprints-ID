package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.exceptions.unexpected.WorkerInjectionFailedError
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

    @Inject lateinit var analyticsManager: AnalyticsManager
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
            val totalCount = countTask.execute(subSyncScope).blockingGet()
            val data = Data.Builder()
                .putInt(key, totalCount.toInt())
                .build()
            toastForDebugBuilds(subSyncScope, data)
            Result.success(data)
        } catch (e: Throwable) {
            e.printStackTrace()
            analyticsManager.logThrowable(e)
            toastForDebugBuilds(subSyncScope)
            Result.success()
        }
    }

    private fun toastForDebugBuilds(subSyncScope: SubSyncScope, data: Data? = null) {
        if (BuildConfig.DEBUG) {
            applicationContext.runOnUiThread {
                val message = "WM - SubCountWorker($subSyncScope): Success - ${data?.keyValueMap}"
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                Timber.d(message)
            }
        }
    }

    private fun inject() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else throw WorkerInjectionFailedError.forWorker<SubCountWorker>()
    }
}
