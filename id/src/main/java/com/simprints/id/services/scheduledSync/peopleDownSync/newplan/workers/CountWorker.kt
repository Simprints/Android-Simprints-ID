package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.services.sync.SyncTaskParameters

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

    companion object {
        const val COUNT_WORKER_TAG = "COUNT_WORKER_TAG"
    }

    override fun doWork(): Result {
    }
}
