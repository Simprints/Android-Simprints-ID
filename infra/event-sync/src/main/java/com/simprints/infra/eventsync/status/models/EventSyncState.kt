package com.simprints.infra.eventsync.status.models

import androidx.annotation.Keep
import com.simprints.core.tools.time.Timestamp

@Keep
data class EventSyncState(
    val syncId: String,
    val progress: Int?,
    val total: Int?,
    val upSyncWorkersInfo: List<SyncWorkerInfo>,
    val downSyncWorkersInfo: List<SyncWorkerInfo>,
    val reporterStates: List<SyncWorkerInfo>,
    val lastSyncTime: Timestamp?,
) {
    data class SyncWorkerInfo(
        val type: EventSyncWorkerType,
        val state: EventSyncWorkerState,
    )

    private val syncWorkersInfo: List<SyncWorkerInfo>
        get() = upSyncWorkersInfo + downSyncWorkersInfo

    fun hasSyncHistory() = syncWorkersInfo
        .isNotEmpty()

    fun isSyncRunning() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Running || it.state is EventSyncWorkerState.Enqueued }

    fun isSyncCompleted() = hasSyncHistory() &&
        syncWorkersInfo.all { it.state is EventSyncWorkerState.Succeeded }

    fun isSyncInProgress() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Running }

    fun isSyncConnecting() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Enqueued }

    fun isSyncFailedBecauseReloginRequired() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseReloginRequired }

    fun isSyncFailedBecauseTooManyRequests() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseTooManyRequest }

    fun isSyncFailedBecauseCloudIntegration() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseCloudIntegration }

    fun isSyncFailedBecauseBackendMaintenance() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseBackendMaintenance }

    fun getEstimatedBackendMaintenanceOutage() = syncWorkersInfo
        .find { it.state is EventSyncWorkerState.Failed && it.state.estimatedOutage != 0L }
        ?.let { it.state as? EventSyncWorkerState.Failed }
        ?.estimatedOutage

    fun isSyncFailedBecauseCommCarePermissionIsMissing() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseCommCarePermissionMissing }

    fun isSyncFailed() = syncWorkersInfo
        .any {
            it.state is EventSyncWorkerState.Failed ||
                it.state is EventSyncWorkerState.Blocked ||
                it.state is EventSyncWorkerState.Cancelled
        }

    fun isSyncReporterCompleted() = reporterStates.isNotEmpty() &&
        reporterStates.all {
            it.state !is EventSyncWorkerState.Running &&
                it.state !is EventSyncWorkerState.Enqueued &&
                it.state !is EventSyncWorkerState.Blocked
        }

    fun isUninitialized(): Boolean = syncId.isBlank() &&
        progress == null &&
        total == null &&
        upSyncWorkersInfo.isEmpty() &&
        downSyncWorkersInfo.isEmpty() &&
        reporterStates.isEmpty() &&
        lastSyncTime == null

    val nonNegativeProgress: Pair<Int, Int>
        get() = (progress?.coerceAtLeast(0) ?: 0) to (total?.coerceAtLeast(0) ?: 0)

    val normalizedProgressProportion: Float
        get() = nonNegativeProgress.let { (current, total) ->
            when {
                isSyncCompleted() -> 1f
                !isSyncInProgress() -> 0f
                total == 0 -> 1f // nothing to sync? - done
                else -> (current.toFloat() / total).coerceIn(0f, 1f)
            }
        }
}
