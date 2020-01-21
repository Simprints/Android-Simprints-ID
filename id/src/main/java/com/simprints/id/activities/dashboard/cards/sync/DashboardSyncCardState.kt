package com.simprints.id.activities.dashboard.cards.sync

import java.util.*

sealed class DashboardSyncCardState(open val lastSyncTime: Date?) {

    data class SyncDefault(override val lastSyncTime: Date?) : DashboardSyncCardState(lastSyncTime)

    data class SyncProgress(override val lastSyncTime: Date?, val progress: Int, val total: Int?) : DashboardSyncCardState(lastSyncTime)
    data class SyncConnecting(override val lastSyncTime: Date?, val progress: Int, val total: Int?) : DashboardSyncCardState(lastSyncTime)

    data class SyncFailed(override val lastSyncTime: Date?) : DashboardSyncCardState(lastSyncTime)
    data class SyncTryAgain(override val lastSyncTime: Date?) : DashboardSyncCardState(lastSyncTime)
    data class SyncComplete(override val lastSyncTime: Date?) : DashboardSyncCardState(lastSyncTime)

    data class SyncNoModules(override val lastSyncTime: Date?) : DashboardSyncCardState(lastSyncTime)
    data class SyncOffline(override val lastSyncTime: Date?) : DashboardSyncCardState(lastSyncTime)
}

