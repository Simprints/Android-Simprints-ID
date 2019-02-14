package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

open class SessionEventsSyncManagerImpl : SessionEventsSyncManager {

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
        return PeriodicWorkRequestBuilder<SessionEventsMasterWorker>(time, unit)
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
        WorkManager.getInstance().cancelUniqueWork(getMasterWorkerUniqueName())
    }

    private fun cancelPreviousVersionedWorkerMaster(){
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
        private const val SYNC_REPEAT_INTERVAL = 1L
        private val SYNC_REPEAT_UNIT = TimeUnit.HOURS

        internal const val MASTER_WORKER_VERSION = 1L
        internal const val MASTER_WORKER_TAG = "SYNC_SESSIONS_WORKER"
    }

    override fun cancelSyncWorkers() {
        WorkManager.getInstance().cancelAllWorkByTag(MASTER_WORKER_TAG)
    }
}
