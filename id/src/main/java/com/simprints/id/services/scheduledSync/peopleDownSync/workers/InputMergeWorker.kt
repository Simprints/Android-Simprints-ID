package com.simprints.id.services.scheduledSync.peopleDownSync.workers

import android.content.Context
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.SaveCountsTask
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class InputMergeWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    @Inject lateinit var saveCountsTask: SaveCountsTask
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder
    @Inject lateinit var analyticsManager: AnalyticsManager

    override fun doWork(): Result {
        inject()
        Timber.d("$inputData")

        // Move from {"subScopeUniqueKey": count} -> {subScope: count}
        val inputForTask = prepareInputForTask()

        return try {
            saveCountsTask.execute(inputForTask)
            outputData = inputData
            Result.SUCCESS
        } catch (e: Throwable) {
            e.printStackTrace()
            analyticsManager.logThrowable(e)
            Result.FAILURE
        }.also {
            if (BuildConfig.DEBUG) {
                applicationContext.runOnUiThread {
                    val message = "WM - InputMergeWorker: $it $inputData"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    Timber.d(message)
                }
            }
        }
    }

    private fun prepareInputForTask(): Map<SubSyncScope, Int> {
        val inputForTask = mutableMapOf<SubSyncScope, Int>()
        val scope = syncScopeBuilder.buildSyncScope()
        scope?.toSubSyncScopes()?.forEach {
            val counter = inputData.getIntArray(it.uniqueKey)?.get(0)
            counter?.let{ counterForSubSync ->
                inputForTask[it] = counterForSubSync
            }
        }
        return inputForTask
    }

    private fun inject() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        } else throw SimprintsError("Cannot get app component in Worker")
    }
}
