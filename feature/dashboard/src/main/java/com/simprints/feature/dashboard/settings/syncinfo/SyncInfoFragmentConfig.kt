package com.simprints.feature.dashboard.settings.syncinfo

data class SyncInfoFragmentConfig(
    val isSyncInfoToolbarVisible: Boolean = true,
    val isSyncInfoStatusHeaderVisible: Boolean = false,
    val isSyncInfoStatusHeaderSettingsButtonVisible: Boolean = false,
    val areSyncInfoSectionHeadersVisible: Boolean = true,
    val isSyncInfoImageSyncVisible: Boolean = true,
    val isSyncInfoRecordsImagesCombined: Boolean = false,
    val isSyncInfoLogoutOnComplete: Boolean = false,
    val isSyncInfoModuleListVisible: Boolean = true,
)
