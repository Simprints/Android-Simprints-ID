package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks.CountTask
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Fabio - Worker to fetch all counters for SyncParams(p, u, arrayOf(m))
 * I: SyncParams
 * O: SyncParams
 * Two possible approaches:
 * a) Use RxJava to execute multiple CountTasks(p, u, m)
 * OR
 * b) zip SubCountWorkers to fetch counter for each (p, u, m)
 */
class CountWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    val projectId by lazy {
        inputData.getString(SyncTaskParameters.PROJECT_ID_FIELD)
            ?: throw IllegalArgumentException("Project Id required")
    }

    val userId by lazy {
        inputData.getString(SyncTaskParameters.USER_ID_FIELD)
    }

    val moduleIds by lazy {
        inputData.getStringArray(SyncTaskParameters.MODULES_ID_FIELD)
    }

    @Inject lateinit var analyticsManager: AnalyticsManager

    companion object {
        const val COUNT_WORKER_TAG = "COUNT_WORKER_TAG"
    }

    override fun doWork(): Result {
        val component = getComponentAndInject()
        val syncScope = SyncScope(projectId, userId, moduleIds?.toSet())
        val subSyncScopes = syncScope.toSubSyncScopes()
        val countTasks = subSyncScopes.map {
            CountTask(component, it.projectId, it.userId, it.moduleId)
                .execute()
                .subscribeOn(Schedulers.newThread())
        }

        return try {
            Completable.mergeDelayError(countTasks).blockingAwait()
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
