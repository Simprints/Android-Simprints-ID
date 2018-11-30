package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.workers

import androidx.work.Worker

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

    companion object {
        const val DOWNSYNC_WORKER_TAG = "DOWNSYNC_WORKER_TAG"
    }

    override fun doWork(): Result {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
