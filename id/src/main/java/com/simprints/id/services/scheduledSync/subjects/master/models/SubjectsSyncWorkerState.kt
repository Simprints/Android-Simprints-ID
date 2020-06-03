package com.simprints.id.services.scheduledSync.subjects.master.models

import androidx.work.WorkInfo

// val state: String is used for logs purpose only - otherwise any state.toString() would print the same output.
sealed class SubjectsSyncWorkerState(val state: String) {

    object Enqueued : SubjectsSyncWorkerState("Enqueued")
    object Running : SubjectsSyncWorkerState("Running")
    object Succeeded : SubjectsSyncWorkerState("Succeeded")
    class Failed(val failedBecauseCloudIntegration: Boolean = false) : SubjectsSyncWorkerState("Failed")
    object Blocked : SubjectsSyncWorkerState("Blocked")
    object Cancelled : SubjectsSyncWorkerState("Cancelled")

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
