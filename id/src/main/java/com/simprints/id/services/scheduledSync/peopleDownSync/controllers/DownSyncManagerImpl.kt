package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.work.*
import androidx.work.WorkInfo.State.RUNNING
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.DOWNSYNC_MASTER_WORKER_TAG_PERIODIC
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.ConstantsWorkManager.Companion.SYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_INTERVAL
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_UNIT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_SYNC_SCOPE_INPUT
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

// Class to enqueue and dequeue DownSyncMasterWorker
class DownSyncManagerImpl(private val syncScopesBuilder: SyncScopesBuilder) : DownSyncManager {

    private val syncScope: SyncScope?
       get() = syncScopesBuilder.buildSyncScope()

    override fun enqueueOneTimeDownSyncMasterWorker() {
        syncScope?.let {
            WorkManager.getInstance().beginUniqueWork(
                DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME,
                ExistingWorkPolicy.KEEP,
                buildOneTimeDownSyncMasterWorker(it)
            ).enqueue()
        }
    }

    override fun enqueuePeriodicDownSyncMasterWorker() {
        syncScope?.let {
            WorkManager.getInstance().enqueueUniquePeriodicWork(
                DOWNSYNC_MASTER_WORKER_TAG_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                buildPeriodicDownSyncMasterWorker(it))
        }
    }

    override fun isDownSyncRunning(): Single<Boolean> = Single.fromCallable {
        syncScope?.let { it ->
            val workersInfo = WorkManager.getInstance().getWorkInfosByTag(SYNC_WORKER_TAG).get()
            workersInfo.find {
                it.state == RUNNING
            } != null
        } ?: false
    }.subscribeOn(AndroidSchedulers.mainThread())

    override fun dequeueAllSyncWorker() {
        WorkManager.getInstance().cancelAllWorkByTag(SYNC_WORKER_TAG)
    }

    override fun buildPeriodicDownSyncMasterWorker(syncScope: SyncScope): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DownSyncMasterWorker>(SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setInputData(getDataForDownSyncMasterWorker(syncScope))
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME)
            .addTag(SYNC_WORKER_TAG)
            .addTag("oneTime")
            .build()



    override fun buildOneTimeDownSyncMasterWorker(syncScope: SyncScope) =
        OneTimeWorkRequestBuilder<DownSyncMasterWorker>()
            .setInputData(getDataForDownSyncMasterWorker(syncScope))
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(DOWNSYNC_MASTER_WORKER_TAG_ONE_TIME)
            .addTag(SYNC_WORKER_TAG)
            .addTag("periodic")
            .build()

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getDataForDownSyncMasterWorker(scope: SyncScope) =
        workDataOf(SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(scope))
}
