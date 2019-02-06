package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import java.util.concurrent.TimeUnit

open class SessionEventsSyncManagerImpl(getWorkManager: () -> WorkManager = WorkManager::getInstance) : SessionEventsSyncManager {

    val workerManager = getWorkManager()

    override fun scheduleSessionsSync() = createAndEnqueueRequest()

    internal fun createAndEnqueueRequest(time: Long = SYNC_REPEAT_INTERVAL,
                                         unit: TimeUnit = SYNC_REPEAT_UNIT,
                                         version: Long = MASTER_WORKER_VERSION) {

        cancelAnyNotVersionedWorkerMasterTask()
        cancelPreviousVersionedWorkerMasterTask()

        val uniqueName = getMasterWorkerUniqueName(version)
        PeriodicWorkRequestBuilder<SessionEventsMasterWorker>(time, unit)
            .setConstraints(getConstraints())
            .addTag(MASTER_WORKER_TAG)
            .build().also {
                WorkManager.getInstance().enqueueUniquePeriodicWork(
                    uniqueName,
                    ExistingPeriodicWorkPolicy.KEEP, it)
            }
    }

    private fun cancelAnyNotVersionedWorkerMasterTask() {
        WorkManager.getInstance().cancelUniqueWork(getMasterWorkerUniqueName())
    }

    private fun cancelPreviousVersionedWorkerMasterTask(){
        ((MASTER_WORKER_VERSION - 1).downTo(0)).forEach {
            WorkManager.getInstance().cancelUniqueWork(getMasterWorkerUniqueName(it))
        }
    }

    internal fun getMasterWorkerUniqueName(version: Long? = null): String =
        version?.let {
            MASTER_WORKER_TAG + "_" + version
        } ?: MASTER_WORKER_TAG

    private fun getConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    companion object {
        private const val SYNC_REPEAT_INTERVAL = 6L
        private val SYNC_REPEAT_UNIT = TimeUnit.HOURS

        internal const val MASTER_WORKER_VERSION = 1L
        internal const val MASTER_WORKER_TAG = "SYNC_SESSIONS_WORKER"
    }

    override fun cancelSyncWorkers() {
        workerManager.cancelAllWorkByTag(MASTER_WORKER_TAG)
        workerManager.cancelAllWorkByTag(SessionEventsSyncMasterTask.SESSIONS_TO_UPLOAD_TAG)
    }
}
