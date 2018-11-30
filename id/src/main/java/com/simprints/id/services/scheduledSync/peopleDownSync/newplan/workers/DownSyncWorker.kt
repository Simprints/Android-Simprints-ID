package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.di.AppComponent
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.tasks.DownSyncTask
import com.simprints.id.services.sync.SyncTaskParameters
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Fabio - Worker to execute syncs for SyncParams(p, u, arrayOf(m))
 * I: SyncParams
 * O: SyncParams
 * Two possible approaches:
 * a) Use RxJava to execute multiple DownSyncTasks(p, u, m)
 * OR
 * b) zip SubDownSyncWorkers to do the sync for each (p, u, m)
 */
class DownSyncWorker : Worker() {

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
        const val DOWNSYNC_WORKER_TAG = "DOWNSYNC_WORKER_TAG"
    }

    override fun doWork(): Result {
        val component = getComponentAndInject()
        val syncScope = SyncScope(projectId, userId, moduleIds?.toSet())
        val subSyncScopes = syncScope.toSubSyncScopes()
        val downSyncTasks = subSyncScopes.map {
            DownSyncTask(component, it.projectId, it.userId, it.moduleId)
                .execute()
                .subscribeOn(Schedulers.newThread())
        }

        return try {
            Completable.mergeDelayError(downSyncTasks).blockingAwait()
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
