package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
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

        val input = inputData.getString(SUBCOUNT_WORKER_SUB_SCOPE_INPUT) ?: throw IllegalArgumentException("input required")
        val subSyncScope = syncScopeBuilder.fromJsonToSubSyncScope(input) ?: throw IllegalArgumentException("SyncScope required")
        val key = subSyncScope.uniqueKey

        return try {
            val totalCount = countTask.execute(subSyncScope).blockingGet()

            outputData = Data.Builder().putInt(key, totalCount.toInt()).build()
            Result.SUCCESS
        } catch (e: Throwable) {
            e.printStackTrace()
            analyticsManager.logThrowable(e)
            Result.SUCCESS
        }.also {
            if (BuildConfig.DEBUG) {
                applicationContext.runOnUiThread {
                    val message = "WM - SubCountWorker($subSyncScope): $it - ${outputData.keyValueMap}"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                    Timber.d(message)
                }
            }
        }
    }

    private fun inject() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else throw SimprintsError("Cannot get app component in Worker")
    }
}
