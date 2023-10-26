package com.simprints.infra.eventsync.status.models

import androidx.annotation.Keep

@Keep
data class EventSyncState(
    val syncId: String,
    val progress: Int,
    val total: Int?,
    val upSyncWorkersInfo: List<SyncWorkerInfo>,
    val downSyncWorkersInfo: List<SyncWorkerInfo>,
) {

    data class SyncWorkerInfo(
        val type: EventSyncWorkerType,
        val state: EventSyncWorkerState,
    )

    fun isThereNotSyncHistory() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .isEmpty()

    fun isSyncRunning() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Running || it.state is EventSyncWorkerState.Enqueued }

    fun isSyncCompleted() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .all { it.state is EventSyncWorkerState.Succeeded }

    fun isSyncInProgress() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Running }

    fun isSyncConnecting() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Enqueued }

    fun isSyncFailedBecauseSignInRequired() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseSignInRequired }

    fun isSyncFailedBecauseTooManyRequests() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseTooManyRequest }

    fun isSyncFailedBecauseCloudIntegration() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseCloudIntegration }

    fun isSyncFailedBecauseBackendMaintenance() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Failed && it.state.failedBecauseBackendMaintenance }

    fun getEstimatedBackendMaintenanceOutage(): Long? {
        val syncWorkerInfo = (upSyncWorkersInfo + downSyncWorkersInfo)
                .find { it.state is EventSyncWorkerState.Failed && it.state.estimatedOutage != 0L }
        val failedWorkerState = syncWorkerInfo?.state as EventSyncWorkerState.Failed?
        return failedWorkerState?.estimatedOutage
    }

    fun isSyncFailed() = (upSyncWorkersInfo + downSyncWorkersInfo)
        .any { it.state is EventSyncWorkerState.Failed || it.state is EventSyncWorkerState.Blocked || it.state is EventSyncWorkerState.Cancelled }

}
