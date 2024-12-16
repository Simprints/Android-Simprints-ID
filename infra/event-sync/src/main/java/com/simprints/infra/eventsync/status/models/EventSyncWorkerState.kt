package com.simprints.infra.eventsync.status.models

import androidx.work.WorkInfo

// val state: String is used for logs purpose only - otherwise any state.toString() would print the same output.
sealed class EventSyncWorkerState(
    val state: String,
) {
    data object Enqueued : EventSyncWorkerState("Enqueued")

    data object Running : EventSyncWorkerState("Running")

    data object Succeeded : EventSyncWorkerState("Succeeded")

    class Failed(
        val failedBecauseReloginRequired: Boolean = false,
        val failedBecauseCloudIntegration: Boolean = false,
        val failedBecauseBackendMaintenance: Boolean = false,
        val failedBecauseTooManyRequest: Boolean = false,
        val estimatedOutage: Long = 0L,
    ) : EventSyncWorkerState("Failed")

    data object Blocked : EventSyncWorkerState("Blocked")

    data object Cancelled : EventSyncWorkerState("Cancelled")

    companion object {
        fun fromWorkInfo(
            state: WorkInfo.State,
            failedBecauseReloginRequired: Boolean = false,
            failedBecauseCloudIntegration: Boolean = false,
            failedBecauseBackendMaintenance: Boolean = false,
            failedBecauseTooManyRequest: Boolean = false,
            estimatedOutage: Long = 0L,
        ) = when (state) {
            WorkInfo.State.ENQUEUED -> Enqueued
            WorkInfo.State.RUNNING -> Running
            WorkInfo.State.SUCCEEDED -> Succeeded
            WorkInfo.State.FAILED -> Failed(
                failedBecauseReloginRequired,
                failedBecauseCloudIntegration,
                failedBecauseBackendMaintenance,
                failedBecauseTooManyRequest,
                estimatedOutage,
            )
            WorkInfo.State.BLOCKED -> Blocked
            WorkInfo.State.CANCELLED -> Cancelled
        }
    }
}
