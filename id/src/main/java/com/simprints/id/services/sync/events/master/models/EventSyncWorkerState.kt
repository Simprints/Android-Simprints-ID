package com.simprints.id.services.sync.events.master.models

import androidx.work.WorkInfo

// val state: String is used for logs purpose only - otherwise any state.toString() would print the same output.
sealed class EventSyncWorkerState(val state: String) {

    object Enqueued : EventSyncWorkerState("Enqueued")
    object Running : EventSyncWorkerState("Running")
    object Succeeded : EventSyncWorkerState("Succeeded")
    class Failed(
        val failedBecauseCloudIntegration: Boolean = false,
        val failedBecauseBackendMaintenance: Boolean = false,
        val failedBecauseTooManyRequest: Boolean = false,
        val estimatedOutage: Long = 0L
    ) :
        EventSyncWorkerState("Failed")

    object Blocked : EventSyncWorkerState("Blocked")
    object Cancelled : EventSyncWorkerState("Cancelled")

    companion object {
        fun fromWorkInfo(
            state: WorkInfo.State,
            failedBecauseCloudIntegration: Boolean = false,
            failedBecauseBackendMaintenance: Boolean = false,
            failedBecauseTooManyRequest: Boolean = false,
            estimatedOutage: Long = 0L
        ) =
            when (state) {
                WorkInfo.State.ENQUEUED -> Enqueued
                WorkInfo.State.RUNNING -> Running
                WorkInfo.State.SUCCEEDED -> Succeeded
                WorkInfo.State.FAILED -> Failed(
                    failedBecauseCloudIntegration,
                    failedBecauseBackendMaintenance,
                    failedBecauseTooManyRequest,
                    estimatedOutage
                )
                WorkInfo.State.BLOCKED -> Blocked
                WorkInfo.State.CANCELLED -> Cancelled
            }
    }
}
