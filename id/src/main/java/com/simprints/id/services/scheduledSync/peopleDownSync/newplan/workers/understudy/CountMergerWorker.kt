package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers.understudy

import androidx.work.Worker

/**
 * Tris - Worker to fetch counter for (p, u, m) using CountTask.
 * Invocated by CountWorker
 */
class CountMergerWorker : Worker() {

    companion object {
        const val COUNT_MERGER_WORKER_TAG = "COUNT_MERGER_WORKER_TAG"
    }
    override fun doWork(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
