package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.services.sync.SyncTaskParameters

/**
 * Fabio - Worker to execute syncs for SyncParams(p, u, arrayOf(m))
 * I: SyncParams
 * O: SyncParams
 * Two possible approaches:
 * a) Use RxJava to execute multiple DownSyncTasks(p, u, m)
 * OR
 * b) zip SubDownSyncWorkers to do the sync for each (p, u, m)
 */
class DownSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

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

    companion object {
        const val DOWNSYNC_WORKER_TAG = "DOWNSYNC_WORKER_TAG"
    }

    override fun doWork(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
