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
import com.simprints.id.services.scheduledSync.peopleDownSync.tasks.DownSyncTask
import org.jetbrains.anko.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

class SubDownSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT = "SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT"

        private const val DEFAULT_COUNTER_FOR_INVALID_VALUE = -1
    }

    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var scopesBuilder: SyncScopesBuilder
    @Inject lateinit var downSyncTask: DownSyncTask

    override fun doWork(): Result {
        inject()

        val input = inputData.getString(SUBDOWNSYNC_WORKER_SUB_SCOPE_INPUT) ?: throw IllegalArgumentException("input required")
        val subSyncScope = scopesBuilder.fromJsonToSubSyncScope(input) ?: throw IllegalArgumentException("SyncScope required")
        val key = subSyncScope.uniqueKey
        val counter = inputData.getIntArray(key)?.get(0) ?: DEFAULT_COUNTER_FOR_INVALID_VALUE

        return try {
            when {
                counter > 0 -> {
                    downSyncTask.execute(subSyncScope).blockingAwait()
                    Result.SUCCESS
                }
                counter == 0 -> {
                    Result.SUCCESS
                }
                else -> throw Throwable("Counter failed for $subSyncScope!") //StopShip: create exception
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            analyticsManager.logThrowable(e)
            Result.FAILURE
        }.also {
            if (BuildConfig.DEBUG) {
                applicationContext.runOnUiThread {
                    val message = "WM - SubDownSyncWorker($subSyncScope): $it"
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
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
