package com.simprints.feature.dashboard.settings.syncinfo

data class SyncInfo(
    val isLoggedIn: Boolean = true,
    val isConfigurationLoadingProgressBarVisible: Boolean = false,
    val isLoginPromptSectionVisible: Boolean = false,
    val syncInfoSectionRecords: SyncInfoSectionRecords = SyncInfoSectionRecords(),
    val syncInfoSectionImages: SyncInfoSectionImages = SyncInfoSectionImages(),
    val syncInfoSectionModules: SyncInfoSectionModules = SyncInfoSectionModules(),
)

data class SyncInfoSectionRecords(
    // counters
    val counterTotalRecords: String = "",
    val counterRecordsToUpload: String = "",
    val isCounterRecordsToDownloadVisible: Boolean = true,
    val counterRecordsToDownload: String = "",
    val isCounterImagesToUploadVisible: Boolean = false, // images may be combined with the records
    val counterImagesToUpload: String = "",

    // instructions
    val isInstructionDefaultVisible: Boolean = false,
    val isInstructionNoModulesVisible: Boolean = false,
    val isInstructionOfflineVisible: Boolean = false,
    val isInstructionErrorVisible: Boolean = false,
    val instructionPopupErrorInfo: SyncInfoError = SyncInfoError(),

    // progress text & progress bar
    val isProgressVisible: Boolean = false,
    val progress: SyncInfoProgress = SyncInfoProgress(),

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

data class SyncInfoError(
    val isBackendMaintenance: Boolean = false,
    val backendMaintenanceEstimatedOutage: Long = -1,
    val isTooManyRequests: Boolean = false,
)

data class SyncInfoSectionImages(
    // counters
    val counterImagesToUpload: String = "",

    // instructions
    val isInstructionDefaultVisible: Boolean = false,
    val isInstructionOfflineVisible: Boolean = false,

    // progress text & progress bar
    val isProgressVisible: Boolean = false,
    val progress: SyncInfoProgress = SyncInfoProgress(),

    // sync button
    val isSyncButtonEnabled: Boolean = false,

    // footer
    val isFooterLastSyncTimeVisible: Boolean = false,
    val footerLastSyncMinutesAgo: String = "",
)

data class SyncInfoProgress(
    val progressParts: List<SyncInfoProgressPart> = listOf(),
    val progressBarPercentage: Int = 0,
)

data class SyncInfoProgressPart(
    val isPending: Boolean = true,
    val isDone: Boolean = false,
    val areNumbersVisible: Boolean = false,
    val currentNumber: Int = 0,
    val totalNumber: Int = 0,
)

data class SyncInfoSectionModules(
    val isSectionAvailable: Boolean = false,
    val moduleCounts: List<SyncInfoModuleCount> = emptyList(),
)

data class SyncInfoModuleCount(
    val isTotal: Boolean = false,
    val name: String,
    val count: String = "",
)
