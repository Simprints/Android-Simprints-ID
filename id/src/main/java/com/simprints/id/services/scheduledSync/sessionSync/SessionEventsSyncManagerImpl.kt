package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import java.util.concurrent.TimeUnit

open class SessionEventsSyncManagerImpl : SessionEventsSyncManager {

    override fun scheduleSessionsSync() = createAndEnqueueRequest()

    private fun createAndEnqueueRequest() {
        PeriodicWorkRequestBuilder<SessionEventsMasterWorker>(SYNC_REPEAT_INTERVAL, SYNC_REPEAT_UNIT)
            .setConstraints(getConstraints())
            .addTag(MASTER_WORKER_TAG)
            .build().also {
                WorkManager.getInstance().enqueueUniquePeriodicWork(MASTER_WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, it)
            }
    }

    private fun getConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    companion object {
        private const val SYNC_REPEAT_INTERVAL = 6L
        private val SYNC_REPEAT_UNIT = TimeUnit.HOURS
        private const val MASTER_WORKER_TAG = "SYNC_SESSIONS_WORKER"
    }

    override fun cancelSyncWorkers() {
        WorkManager.getInstance().cancelAllWorkByTag(MASTER_WORKER_TAG)
        WorkManager.getInstance().cancelAllWorkByTag(SessionEventsSyncMasterTask.SESSIONS_TO_UPLOAD_TAG)
    }
}
