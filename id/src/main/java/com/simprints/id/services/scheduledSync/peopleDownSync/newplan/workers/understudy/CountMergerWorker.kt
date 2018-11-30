package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Tris - Worker to fetch counter for (p, u, m) using CountTask.
 * Invocated by CountWorker
 */
class CountMergerWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    companion object {
        const val COUNT_MERGER_WORKER_TAG = "COUNT_MERGER_WORKER_TAG"
    }
    override fun doWork(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
