package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

open class SessionEventsSyncManagerImpl(private val workManager: WorkManager) : SessionEventsSyncManager {

    override fun scheduleSessionsSync() {
        createAndEnqueueRequest()
    }

    internal fun createAndEnqueueRequest(time: Long = SYNC_REPEAT_INTERVAL,
                                         unit: TimeUnit = SYNC_REPEAT_UNIT,
                                         version: Long = MASTER_WORKER_VERSION,
                                         tag: String = MASTER_WORKER_TAG): UUID {

        cancelAnyNotVersionedWorkerMaster()
        cancelPreviousVersionedWorkerMaster()

        val uniqueName = getMasterWorkerUniqueName(version)
        return PeriodicWorkRequestBuilder<UpSessionEventsWorker>(time, unit)
            .setConstraints(getConstraints())
            .addTag(tag)
            .build().let {
                WorkManager.getInstance().enqueueUniquePeriodicWork(
                    uniqueName,
                    ExistingPeriodicWorkPolicy.KEEP, it)
                it.id
            }
    }

    private fun cancelAnyNotVersionedWorkerMaster() {
        workManager.cancelUniqueWork(getMasterWorkerUniqueName())
    }

    private fun cancelPreviousVersionedWorkerMaster(){
        ((MASTER_WORKER_VERSION - 1).downTo(0)).forEach {
            workManager.cancelUniqueWork(getMasterWorkerUniqueName(it))
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
        private const val SYNC_REPEAT_INTERVAL = 15L
        private val SYNC_REPEAT_UNIT = TimeUnit.MINUTES

        internal const val MASTER_WORKER_VERSION = 1L
        internal const val MASTER_WORKER_TAG = "SYNC_SESSIONS_WORKER"
    }

    override fun cancelSyncWorkers() {
        workManager.cancelAllWorkByTag(MASTER_WORKER_TAG)
    }
}
