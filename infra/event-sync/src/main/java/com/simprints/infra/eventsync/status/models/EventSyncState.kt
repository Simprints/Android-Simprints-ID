package com.simprints.infra.eventsync.status.models

import androidx.annotation.Keep

@Keep
data class EventSyncState(
    val syncId: String,
    val progress: Int?,
    val total: Int?,
    val upSyncWorkersInfo: List<SyncWorkerInfo>,
    val downSyncWorkersInfo: List<SyncWorkerInfo>,
    val reporterStates: List<SyncWorkerInfo>,
) {
    data class SyncWorkerInfo(
        val type: EventSyncWorkerType,
        val state: EventSyncWorkerState,
    )

    private val syncWorkersInfo: List<SyncWorkerInfo>
        get() = upSyncWorkersInfo + downSyncWorkersInfo

    fun isThereNotSyncHistory() = syncWorkersInfo
        .isEmpty()

    fun isSyncRunning() = syncWorkersInfo
        .any { it.state is EventSyncWorkerState.Running || it.state is EventSyncWorkerState.Enqueued }

    fun isSyncCompleted() = syncWorkersInfo
        .all { it.state is EventSyncWorkerState.Succeeded }

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

    fun isSyncFailed() = syncWorkersInfo
        .any {
            it.state is EventSyncWorkerState.Failed ||
                it.state is EventSyncWorkerState.Blocked ||
                it.state is EventSyncWorkerState.Cancelled
        }

    fun isSyncReporterCompleted() = reporterStates
        .all {
            it.state !is EventSyncWorkerState.Running &&
                it.state !is EventSyncWorkerState.Enqueued &&
                it.state !is EventSyncWorkerState.Blocked
        }
}
