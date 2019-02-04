package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import java.util.concurrent.TimeUnit

open class SessionEventsSyncManagerImpl : SessionEventsSyncManager {

    override fun scheduleSessionsSync() = createAndEnqueueRequest()

    private fun createAndEnqueueRequest(time: Long = SYNC_REPEAT_INTERVAL,
                                        unit: TimeUnit = SYNC_REPEAT_UNIT,
                                        tag: String = MASTER_WORKER_TAG) {
        PeriodicWorkRequestBuilder<SessionEventsMasterWorker>(time, unit)
            .setConstraints(getConstraints())
            .addTag(tag)
            .build().also {
                WorkManager.getInstance().enqueueUniquePeriodicWork(tag, ExistingPeriodicWorkPolicy.KEEP, it)
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
