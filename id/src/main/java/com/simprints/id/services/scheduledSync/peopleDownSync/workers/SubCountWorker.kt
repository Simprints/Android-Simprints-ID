package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.widget.Toast
import androidx.work.Data
import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.CountTask
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class SubCountWorker : Worker() {

    companion object {
        const val SUBCOUNT_WORKER_SUB_SCOPE_INPUT = "SUBCOUNT_WORKER_SUB_SCOPE_INPUT"
    }

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder

    override fun doWork(): Result {
        getComponentAndInject()

        val input = inputData.getString(SUBCOUNT_WORKER_SUB_SCOPE_INPUT) ?: throw IllegalArgumentException("input required")
        val subSyncScope = syncScopeBuilder.fromJsonToSubSyncScope(input)  ?: throw IllegalArgumentException("SyncScope required")
        val key = subSyncScope.uniqueKey
        val component = getComponentAndInject()

        return try {
            val totalCount = CountTask(component, subSyncScope).execute().blockingGet()

            outputData = Data.Builder().putInt(key, totalCount.toInt()).build()
            Result.SUCCESS
        } catch (e: Throwable) {
            analyticsManager.logThrowable(e)
            Result.FAILURE
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

    private fun getComponentAndInject(): AppComponent {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
            return context.component
        } else throw SimprintsError("Cannot get app component in Worker")
    }
}
