package com.simprints.infra.eventsync.status.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp

@Keep
data class DownSyncState(
    val syncId: String,
    val progress: Int?,
    val total: Int?,
    val workersInfo: List<EventSyncState.SyncWorkerInfo>,
    val lastSyncTime: Timestamp?,
) {
    fun hasSyncHistory() = workersInfo.isNotEmpty()

    fun isSyncRunning() = workersInfo.any {
        it.state is EventSyncWorkerState.Running || it.state is EventSyncWorkerState.Enqueued
    }

    fun isSyncCompleted() = hasSyncHistory() && workersInfo.all { it.state is EventSyncWorkerState.Succeeded }

    fun isSyncInProgress() = workersInfo.any { it.state is EventSyncWorkerState.Running }

    fun isSyncConnecting() = workersInfo.any { it.state is EventSyncWorkerState.Enqueued }

    fun isSyncFailed() = workersInfo.any {
        it.state is EventSyncWorkerState.Failed ||
            it.state is EventSyncWorkerState.Blocked ||
            it.state is EventSyncWorkerState.Cancelled
    }

    fun isSyncFailedBecauseReloginRequired() = workersInfo.any {
        it.state is EventSyncWorkerState.Failed && it.state.failedBecauseReloginRequired
    }

    fun isSyncFailedBecauseTooManyRequests() = workersInfo.any {
        it.state is EventSyncWorkerState.Failed && it.state.failedBecauseTooManyRequest
    }

    fun isSyncFailedBecauseBackendMaintenance() = workersInfo.any {
        it.state is EventSyncWorkerState.Failed && it.state.failedBecauseBackendMaintenance
    }

    fun isSyncFailedBecauseCommCarePermissionIsMissing() = workersInfo.any {
        it.state is EventSyncWorkerState.Failed && it.state.failedBecauseCommCarePermissionMissing
    }

    fun isSyncFailedBecauseCloudIntegration() = workersInfo.any {
        it.state is EventSyncWorkerState.Failed && it.state.failedBecauseCloudIntegration
    }

    fun getEstimatedBackendMaintenanceOutage() = workersInfo
        .find { it.state is EventSyncWorkerState.Failed && it.state.estimatedOutage != 0L }
        ?.let { it.state as? EventSyncWorkerState.Failed }
        ?.estimatedOutage

    fun isUninitialized() = syncId.isBlank() &&
        progress == null &&
        total == null &&
        workersInfo.isEmpty() &&
        lastSyncTime == null

    val nonNegativeProgress: Pair<Int, Int>
        get() = (progress?.coerceAtLeast(0) ?: 0) to (total?.coerceAtLeast(0) ?: 0)

    val normalizedProgressProportion: Float
        get() = nonNegativeProgress.let { (current, total) ->
            when {
                isSyncCompleted() -> 1f
                !isSyncInProgress() -> 0f
                total == 0 -> 1f
                else -> (current.toFloat() / total).coerceIn(0f, 1f)
            }
        }
}
