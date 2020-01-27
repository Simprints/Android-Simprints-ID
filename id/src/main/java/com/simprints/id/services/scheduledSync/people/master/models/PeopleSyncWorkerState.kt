package com.simprints.id.services.scheduledSync.people.master.models

import androidx.work.WorkInfo

// val state: String is used for logs purpose only - otherwise any state.toString() would print the same output.
sealed class PeopleSyncWorkerState(val state: String) {

    object Enqueued : PeopleSyncWorkerState("Enqueued")
    object Running : PeopleSyncWorkerState("Running")
    object Succeeded : PeopleSyncWorkerState("Succeeded")
    class Failed(val failedBecauseCloudIntegration: Boolean = false) : PeopleSyncWorkerState("Failed")
    object Blocked : PeopleSyncWorkerState("Blocked")
    object Cancelled : PeopleSyncWorkerState("Cancelled")

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
