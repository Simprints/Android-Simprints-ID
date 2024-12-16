package com.simprints.feature.dashboard.views

internal sealed class SyncCardState(
    open val lastTimeSyncSucceed: String?,
) {
    data class SyncDefault(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncPendingUpload(
        override val lastTimeSyncSucceed: String?,
        val itemsToUpSync: Int,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncProgress(
        override val lastTimeSyncSucceed: String?,
        val progress: Int?,
        val total: Int?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncConnecting(
        override val lastTimeSyncSucceed: String?,
        val progress: Int?,
        val total: Int?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncFailed(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncFailedReloginRequired(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncFailedBackendMaintenance(
        override val lastTimeSyncSucceed: String?,
        val estimatedOutage: Long? = null,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncTooManyRequests(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncTryAgain(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncComplete(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncHasNoModules(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)

    data class SyncOffline(
        override val lastTimeSyncSucceed: String?,
    ) : SyncCardState(lastTimeSyncSucceed)
}
