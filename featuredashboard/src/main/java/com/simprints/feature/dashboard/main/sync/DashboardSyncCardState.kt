package com.simprints.feature.dashboard.main.sync

internal sealed class DashboardSyncCardState(open val lastTimeSyncSucceed: String?) {

    data class SyncDefault(override val lastTimeSyncSucceed: String?) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncProgress(
        override val lastTimeSyncSucceed: String?,
        val progress: Int,
        val total: Int?
    ) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncConnecting(
        override val lastTimeSyncSucceed: String?,
        val progress: Int,
        val total: Int?
    ) : DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncFailed(override val lastTimeSyncSucceed: String?) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncFailedBackendMaintenance(
        override val lastTimeSyncSucceed: String?,
        val estimatedOutage: Long? = null
    ) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncTooManyRequests(override val lastTimeSyncSucceed: String?) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncTryAgain(override val lastTimeSyncSucceed: String?) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncComplete(override val lastTimeSyncSucceed: String?) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncHasNoModules(override val lastTimeSyncSucceed: String?) :
        DashboardSyncCardState(lastTimeSyncSucceed)

    data class SyncOffline(override val lastTimeSyncSucceed: String?) :
        DashboardSyncCardState(lastTimeSyncSucceed)
}

