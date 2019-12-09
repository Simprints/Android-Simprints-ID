package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.*
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncState
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_INTERVAL
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_REPEAT_UNIT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.DownSyncMasterWorker.Companion.SYNC_WORKER_SYNC_SCOPE_INPUT
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.DOWNSYNC_MASTER_WORKER_ONE_TIME_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.DOWNSYNC_MASTER_WORKER_PERIODIC_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.DOWNSYNC_WORKER_CHAIN_UNIQUE_NAME
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.SUBDOWNSYNC_WORKER_TAG
import com.simprints.id.services.scheduledSync.peopleDownSync.workers.WorkManagerConstants.Companion.SYNC_WORKER_TAG

// Class to enqueue and dequeue DownSyncMasterWorker
class DownSyncManagerImpl(private val ctx: Context,
                          private val syncScopesBuilder: SyncScopesBuilder) : DownSyncManager {

    private val syncScope: SyncScope?
        get() = syncScopesBuilder.buildSyncScope()

    private val wm: WorkManager
        get() = WorkManager.getInstance(ctx)

    override fun enqueueOneTimeDownSync() {
        syncScope?.let {
            wm.beginUniqueWork(
                DOWNSYNC_WORKER_CHAIN_UNIQUE_NAME,
                ExistingWorkPolicy.KEEP,
                buildOneTimeDownSyncMasterWorker(it)
            ).enqueue()
        }
    }

    override fun enqueuePeriodicDownSync() {
        syncScope?.let {
            wm.enqueueUniquePeriodicWork(
                DOWNSYNC_MASTER_WORKER_PERIODIC_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                buildPeriodicDownSyncMasterWorker(it))
        }
    }

    override fun onSyncStateUpdated(): LiveData<SyncState> {
        val liveDataSyncWorkersInfo = WorkManager.getInstance().getWorkInfosByTagLiveData(SUBDOWNSYNC_WORKER_TAG)

        return Transformations.map(liveDataSyncWorkersInfo) {

            val isSyncCounting = it.any { workInfo -> workInfo.state == WorkInfo.State.BLOCKED }
            val isSyncRunning = it.any { workInfo -> workInfo.state == WorkInfo.State.RUNNING }
            when {
                isSyncCounting -> SyncState.CALCULATING
                isSyncRunning -> SyncState.RUNNING
                else -> SyncState.NOT_RUNNING
            }
        }
    }

    override fun dequeueAllSyncWorker() {
        WorkManager.getInstance().cancelAllWorkByTag(SYNC_WORKER_TAG)
    }

    override fun buildPeriodicDownSyncMasterWorker(syncScope: SyncScope): PeriodicWorkRequest =
        PeriodicWorkRequest.Builder(DownSyncMasterWorker::class.java, SYNC_WORKER_REPEAT_INTERVAL, SYNC_WORKER_REPEAT_UNIT)
            .setInputData(getDataForDownSyncMasterWorker(syncScope))
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(DOWNSYNC_MASTER_WORKER_ONE_TIME_TAG)
            .addTag(SYNC_WORKER_TAG)
            .addTag("oneTime")
            .build()


    override fun buildOneTimeDownSyncMasterWorker(syncScope: SyncScope) {
        val subSyncScopes = syncScope.toSubSyncScopes()
        val firstSubSyncScope = subSyncScopes.first()

        val chainDownSync = wm.beginWith(downSyncTask(firstSubSyncScope))
        subSyncScopes.drop(1).forEach {
            chainDownSync.then(downSyncTask(it))
        }

        val chainCount = wm.beginWith(countTask(syncScope))
        val chain = WorkContinuation.combine(listOf(chainDownSync, chainCount))

        wm.beginUniqueWork(DOWNSYNC_WORKER_CHAIN_UNIQUE_NAME, ExistingWorkPolicy.KEEP, chain)

        chain.enqueue()
    }

    private fun countTask(syncScope: SyncScope): OneTimeWorkRequest {

    }

    private fun downSyncTask(subSyncScope: SubSyncScope?): OneTimeWorkRequest {
        OneTimeWorkRequest.Builder(DownSyncMasterWorker::class.java)
            .setInputData(getDataForDownSyncMasterWorker(syncScope))
            .setConstraints(getDownSyncMasterWorkerConstraints())
            .addTag(DOWNSYNC_MASTER_WORKER_ONE_TIME_TAG)
            .addTag(SYNC_WORKER_TAG)
            .addTag("periodic")
            .build()
    }

    private fun getDownSyncMasterWorkerConstraints() =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    private fun getDataForDownSyncMasterWorker(scope: SyncScope) =
        workDataOf(SYNC_WORKER_SYNC_SCOPE_INPUT to syncScopesBuilder.fromSyncScopeToJson(scope))
}
