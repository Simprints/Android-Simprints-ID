package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks.DownSyncTask
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.json.JsonHelper
import java.util.*
import javax.inject.Inject

/**
 * Tris - Worker to execute sync for (p, u, m) using DownSyncTask.
 * Invocated by DownSyncWorker
 */
class SubDownSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    val projectId by lazy {
        inputData.getString(SyncTaskParameters.PROJECT_ID_FIELD)
            ?: throw IllegalArgumentException("Project Id required")
    }

    val userId by lazy {
        inputData.getString(SyncTaskParameters.USER_ID_FIELD)
    }

    val moduleId by lazy {
        inputData.getString(SyncTaskParameters.MODULE_ID_FIELD)
    }

    @Inject lateinit var analyticsManager: AnalyticsManager

    companion object {
        const val SUBDOWNSYNC_WORKER_TAG = "SUBDOWNSYNC_WORKER_TAG"
    }
    override fun doWork(): Result {
        Log.d("WM", "Running SubDownSyncWorker with ${JsonHelper.toJson(inputData)}")
        Thread.sleep(Random().nextInt(2) * 1000L)
        return Result.SUCCESS
    }

    fun actual_doWork(): Result {
        val component = getComponentAndInject()

        return try {
            DownSyncTask(component, projectId, userId, moduleId).execute().blockingAwait()
            Result.SUCCESS
        } catch (e: Throwable) {
            analyticsManager.logThrowable(e)
            Result.FAILURE
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
