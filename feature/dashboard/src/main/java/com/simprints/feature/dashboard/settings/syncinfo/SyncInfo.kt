package com.simprints.feature.dashboard.settings.syncinfo

import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount

internal data class SyncInfo(
    val isLoggedIn: Boolean = true,
    val isConfigurationLoadingProgressBarVisible: Boolean = false,
    val isLoginPromptSectionVisible: Boolean = false,
    val isImageSyncSectionVisible: Boolean = false,
    val syncInfoSectionRecords: SyncInfoSectionRecords = SyncInfoSectionRecords(),
    val syncInfoSectionImages: SyncInfoSectionImages = SyncInfoSectionImages(),
    val syncInfoSectionModules: SyncInfoSectionModules = SyncInfoSectionModules(),
)

internal enum class RecordSyncVisibleState {
    NOTHING,
    ON_STANDBY,
    IN_PROGRESS,
    COMM_CARE_ERROR,
    NO_MODULES_ERROR,
    OFFLINE_ERROR,
    ERROR,
}

internal data class SyncInfoSectionRecords(
    // counters
    val counterTotalRecords: String = "",
    val counterRecordsToUpload: String = "",
    val isCounterRecordsToDownloadVisible: Boolean = true,
    val counterRecordsToDownload: String = "",
    val isCounterImagesToUploadVisible: Boolean = false, // images may be combined with the records
    val counterImagesToUpload: String = "",
    // instructions
    val recordSyncVisibleState: RecordSyncVisibleState = RecordSyncVisibleState.NOTHING,
    val instructionPopupErrorInfo: SyncInfoError = SyncInfoError(),
    // progress text & progress bar
    val isProgressVisible: Boolean = false,
    val progress: SyncProgressInfo = SyncProgressInfo(),
    // sync button
    val isSyncButtonVisible: Boolean = false,
    val isSyncButtonEnabled: Boolean = false,
    val isSyncButtonForRetry: Boolean = false,
    // footer
    val isFooterSyncInProgressVisible: Boolean = true,
    val isFooterReadyToLogOutVisible: Boolean = false,
    val isFooterSyncIncompleteVisible: Boolean = false,
    val isFooterLastSyncTimeVisible: Boolean = false,
    val footerLastSyncMinutesAgo: String = "",
)

internal data class SyncInfoError(
    val isBackendMaintenance: Boolean = false,
    val backendMaintenanceEstimatedOutage: Long = -1,
    val isTooManyRequests: Boolean = false,
)

internal data class SyncInfoSectionImages(
    // counters
    val counterImagesToUpload: String = "",
    // instructions
    val isInstructionDefaultVisible: Boolean = false,
    val isInstructionOfflineVisible: Boolean = false,
    // progress text & progress bar
    val isProgressVisible: Boolean = false,
    val progress: SyncProgressInfo = SyncProgressInfo(),
    // sync button
    val isSyncButtonEnabled: Boolean = false,
    // footer
    val isFooterLastSyncTimeVisible: Boolean = false,
    val footerLastSyncMinutesAgo: String = "",
)

internal data class SyncProgressInfo(
    val progressParts: List<SyncProgressInfoPart> = listOf(),
    val progressBarPercentage: Int = 0,
)

internal data class SyncProgressInfoPart(
    val isPending: Boolean = true,
    val isDone: Boolean = false,
    val areNumbersVisible: Boolean = false,
    val currentNumber: Int = 0,
    val totalNumber: Int = 0,
)

internal data class SyncInfoSectionModules(
    val isSectionAvailable: Boolean = false,
    val moduleCounts: List<ModuleCount> = emptyList(),
)

internal enum class LogoutActionReason {
    USER_ACTION,
    PROJECT_ENDING_OR_DEVICE_COMPROMISED,
}
