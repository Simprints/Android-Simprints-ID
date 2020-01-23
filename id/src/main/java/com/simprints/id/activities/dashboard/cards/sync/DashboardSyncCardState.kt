package com.simprints.id.activities.dashboard.cards.sync

import java.util.*

sealed class DashboardSyncCardState(open val lastTimeSyncSucceed: Date?) {

    data class SyncDefault(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncProgress(override val lastTimeSyncSucceed: Date?, val progress: Int, val total: Int?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncConnecting(override val lastTimeSyncSucceed: Date?, val progress: Int, val total: Int?) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncFailed(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncTryAgain(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncComplete(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncNoModules(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
    data class SyncOffline(override val lastTimeSyncSucceed: Date?) : DashboardSyncCardState(lastTimeSyncSucceed)
}

