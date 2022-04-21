package com.simprints.id.activities.dashboard.cards.sync

import java.util.Date

sealed class DashboardSyncCardState(open val lastTimeSyncSucceed: Date?) {

    data class SyncDefault(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncProgress(override val lastTimeSyncSucceed: Date?, val progress: Int, val total: Int?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncConnecting(override val lastTimeSyncSucceed: Date?, val progress: Int, val total: Int?) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncFailed(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncFailedBackendMaintenance(override val lastTimeSyncSucceed: Date?, val estimatedOutage: Long? = null) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncTryAgain(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncComplete(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncHasNoModules(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncOffline(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
}

