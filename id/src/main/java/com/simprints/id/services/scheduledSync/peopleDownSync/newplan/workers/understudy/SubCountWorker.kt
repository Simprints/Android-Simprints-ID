package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks.CountTask
import com.simprints.id.services.sync.SyncTaskParameters
import com.simprints.id.tools.json.JsonHelper
import java.util.*
import javax.inject.Inject

/**
 * Tris - Worker to fetch counter for (p, u, m) using CountTask.
 * Invocated by CountWorker
 */
class SubCountWorker : Worker() {

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
        const val SUBCOUNT_WORKER_TAG = "SUBCOUNT_WORKER_TAG"
    }

    override fun doWork(): Result {
        Thread.sleep(Random().nextInt(2) * 1000L)

        return if (Random().nextBoolean()) {
            val total = Random().nextInt(100)
            Log.d("WM", "Succeed SubCountWorker with ${JsonHelper.toJson(inputData)}. Fetched: $total")

            //val input = inputData.keyValueMap.toMutableMap()
            //input["total"] = total
            outputData = Data.Builder().putAll(inputData).put("total", total).build()
            Result.SUCCESS
        } else {
            Log.d("WM", "Failed SubCountWorker with ${JsonHelper.toJson(inputData)}")
            Result.SUCCESS
        }
    }

    fun actual_doWork(): Result {
        val component = getComponentAndInject()

        return try {
            CountTask(component, projectId, userId, moduleId).execute().blockingAwait()
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
