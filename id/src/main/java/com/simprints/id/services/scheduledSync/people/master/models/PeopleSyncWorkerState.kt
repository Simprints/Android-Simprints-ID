package com.simprints.id.services.scheduledSync.people.master.models

import androidx.work.WorkInfo

sealed class PeopleSyncWorkerState {
    object Enqueued : PeopleSyncWorkerState()
    object Running : PeopleSyncWorkerState()
    object Succeeded : PeopleSyncWorkerState()
    class Failed(val failedBecauseCloudIntegration: Boolean = false) : PeopleSyncWorkerState()
    object Blocked : PeopleSyncWorkerState()
    object Cancelled : PeopleSyncWorkerState()

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
