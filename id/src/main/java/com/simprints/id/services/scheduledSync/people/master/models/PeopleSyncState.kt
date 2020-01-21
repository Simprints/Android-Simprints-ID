package com.simprints.id.services.scheduledSync.people.master

import androidx.annotation.Keep
import androidx.work.WorkInfo

@Keep
class PeopleSyncState(val syncId: String,
                      val progress: Int,
                      val total: Int?,
                      val upSyncWorkersInfo: List<SyncWorkerInfo>,
                      val downSyncWorkersInfo: List<SyncWorkerInfo>) {

    data class SyncWorkerInfo(val type: PeopleSyncWorkerType,
                              val state: WorkerState)

}

sealed class WorkerState {
    object Enqueued : WorkerState()
    object Running : WorkerState()
    object Succeeded : WorkerState()
    class Failed(val failedBecauseCloudIntegration: Boolean) : WorkerState()
    object Blocked : WorkerState()
    object Cancelled : WorkerState()

    companion object {
        fun fromWorkInfo(state: WorkInfo.State,
                         failedBecauseCloudIntegration: Boolean = false) =
            when (state) {
                WorkInfo.State.ENQUEUED -> Enqueued
                WorkInfo.State.RUNNING -> Running
                WorkInfo.State.SUCCEEDED -> Succeeded
                WorkInfo.State.FAILED -> Failed(failedBecauseCloudIntegration)
                WorkInfo.State.BLOCKED -> Blocked
                WorkInfo.State.CANCELLED -> Cancelled
            }
    }
}
