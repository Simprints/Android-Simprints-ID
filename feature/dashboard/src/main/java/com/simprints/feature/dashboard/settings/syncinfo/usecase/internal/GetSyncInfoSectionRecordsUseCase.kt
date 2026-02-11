package com.simprints.feature.dashboard.settings.syncinfo.usecase.internal

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.RecordSyncVisibleState
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoError
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionRecords
import com.simprints.feature.dashboard.settings.syncinfo.SyncProgressInfo
import com.simprints.feature.dashboard.settings.syncinfo.SyncProgressInfoPart
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.permission.CommCarePermissionChecker
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.sync.ImageSyncStatus
import com.simprints.infra.sync.SyncableCounts
import javax.inject.Inject
import kotlin.math.roundToInt

internal class GetSyncInfoSectionRecordsUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val commCarePermissionChecker: CommCarePermissionChecker,
) {
    operator fun invoke(
        isPreLogoutUpSync: Boolean,
        isOnline: Boolean,
        projectId: String,
        eventSyncState: EventSyncState,
        imageSyncStatus: ImageSyncStatus,
        syncableCounts: SyncableCounts,
        isProjectRunning: Boolean,
        moduleCounts: List<ModuleCount>,
        projectConfig: ProjectConfiguration,
    ): SyncInfoSectionRecords {
        val isSyncInProgress = eventSyncState.isSyncInProgress() ||
            (isPreLogoutUpSync && imageSyncStatus.isSyncing) // also in progress if combined with image sync

        val counterTotalRecords = getRecordsTotal(isSyncInProgress, projectId, syncableCounts)
        val counterRecordsToDownload = getRecordsToDownload(isSyncInProgress, isPreLogoutUpSync, projectConfig, syncableCounts)
        val counterRecordsToUpload = getRecordsToUpload(isSyncInProgress, syncableCounts)
        val counterImagesToUpload = getImagesToUpload(imageSyncStatus, syncableCounts)

        val progress = getSyncProgressInfo(isSyncInProgress, isPreLogoutUpSync, eventSyncState, imageSyncStatus, syncableCounts)

        val recordSyncVisibleState =
            getSyncVisibleState(isSyncInProgress, isOnline, isPreLogoutUpSync, projectConfig, moduleCounts, eventSyncState)

        val isSyncButtonEnabled = isSyncButtonEnabled(isOnline, isPreLogoutUpSync, projectConfig, eventSyncState, recordSyncVisibleState)

        return SyncInfoSectionRecords(
            counterTotalRecords,
            counterRecordsToUpload,
            isCounterRecordsToDownloadVisible = !isPreLogoutUpSync && isProjectRunning,
            counterRecordsToDownload,
            isCounterImagesToUploadVisible = isPreLogoutUpSync,
            counterImagesToUpload,
            recordSyncVisibleState,
            instructionPopupErrorInfo = SyncInfoError(
                isBackendMaintenance = eventSyncState.isSyncFailedBecauseBackendMaintenance(),
                backendMaintenanceEstimatedOutage = eventSyncState.getEstimatedBackendMaintenanceOutage() ?: -1,
                isTooManyRequests = eventSyncState.isSyncFailedBecauseTooManyRequests(),
            ),
            isProgressVisible = recordSyncVisibleState == RecordSyncVisibleState.IN_PROGRESS,
            progress,
            isSyncButtonVisible = !isPreLogoutUpSync || eventSyncState.isSyncFailed(),
            isSyncButtonEnabled,
            isSyncButtonForRetry = eventSyncState.isSyncFailed(),
            isFooterSyncInProgressVisible = isPreLogoutUpSync && isSyncInProgress,
            isFooterReadyToLogOutVisible = isPreLogoutUpSync && eventSyncState.isSyncCompleted() && !imageSyncStatus.isSyncing,
            isFooterSyncIncompleteVisible = isPreLogoutUpSync && eventSyncState.isSyncFailed(),
            isFooterLastSyncTimeVisible =
                !isPreLogoutUpSync && !eventSyncState.isSyncInProgress() && eventSyncState.lastSyncTimestamp.ms >= 0,
            footerLastSyncMinutesAgo = timeHelper.readableBetweenNowAndTime(eventSyncState.lastSyncTimestamp),
        )
    }

    private fun getRecordsToDownload(
        isSyncInProgress: Boolean,
        isPreLogoutUpSync: Boolean,
        projectConfig: ProjectConfiguration,
        syncableCounts: SyncableCounts,
    ): String = when {
        isSyncInProgress || isPreLogoutUpSync -> null
        projectConfig.isSimprintsEventDownSyncAllowed() -> with(syncableCounts) {
            DownSyncCounts(recordEventsToDownload, isRecordEventsToDownloadLowerBound)
        }
        else -> DownSyncCounts(0, isLowerBound = false)
    }?.let { "${it.count}${if (it.isLowerBound) "+" else ""}" }.orEmpty()

    private fun getRecordsTotal(
        isSyncInProgress: Boolean,
        projectId: String,
        syncableCounts: SyncableCounts,
    ): String {
        val recordsTotal = when {
            isSyncInProgress || projectId.isBlank() -> null
            else -> syncableCounts.totalRecords
        }
        return recordsTotal?.toString().orEmpty()
    }

    private fun getRecordsToUpload(
        isSyncInProgress: Boolean,
        syncableCounts: SyncableCounts,
    ): String {
        val recordsToUpload = when {
            isSyncInProgress -> null
            else -> syncableCounts.enrolmentsToUpload
        }
        return recordsToUpload?.toString().orEmpty()
    }

    private fun getImagesToUpload(
        imageSyncStatus: ImageSyncStatus,
        syncableCounts: SyncableCounts,
    ): String {
        val imagesToUpload = if (imageSyncStatus.isSyncing) {
            null
        } else {
            syncableCounts.samplesToUpload // internal term is sample, user-facing (within sync info) term is image
        }
        return imagesToUpload?.toString().orEmpty()
    }

    private fun getSyncProgressInfo(
        isSyncInProgress: Boolean,
        isPreLogoutUpSync: Boolean,
        eventSyncState: EventSyncState,
        imageSyncStatus: ImageSyncStatus,
        syncableCounts: SyncableCounts,
    ): SyncProgressInfo {
        if (!isSyncInProgress) {
            return SyncProgressInfo()
        }
        val combinedProgressProportion = getCombinedProgressProportion(
            isPreLogoutUpSync,
            eventSyncState,
            imageSyncStatus,
        )
        val (currentEvents, totalEvents) = eventSyncState.nonNegativeProgress
        val (currentImages, totalImages) = imageSyncStatus.nonNegativeProgress

        val eventSyncProgressPart = SyncProgressInfoPart(
            isPending = eventSyncState.isSyncConnecting() || !eventSyncState.hasSyncHistory(),
            isDone = eventSyncState.isSyncCompleted(),
            areNumbersVisible = eventSyncState.isSyncInProgress() && totalEvents > 0,
            currentNumber = currentEvents,
            totalNumber = totalEvents,
        )
        val imageSyncProgressPart = SyncProgressInfoPart(
            isPending = eventSyncState.isSyncInProgress() && !imageSyncStatus.isSyncing,
            isDone = !eventSyncState.isSyncInProgress() && !imageSyncStatus.isSyncing && syncableCounts.samplesToUpload == 0,
            areNumbersVisible = imageSyncStatus.isSyncing && totalImages > 0,
            currentNumber = currentImages,
            totalNumber = totalImages,
        )
        return SyncProgressInfo(
            progressParts = if (isPreLogoutUpSync) {
                listOf(eventSyncProgressPart, imageSyncProgressPart)
            } else {
                listOf(eventSyncProgressPart)
            },
            progressBarPercentage = (combinedProgressProportion * 100).roundToInt(),
        )
    }

    private fun getCombinedProgressProportion(
        isPreLogoutUpSync: Boolean,
        eventSyncState: EventSyncState,
        imageSyncStatus: ImageSyncStatus,
    ): Float {
        val eventPartProgressProportion = eventSyncState.normalizedProgressProportion
        val imagePartProgressProportion = imageSyncStatus.normalizedProgressProportion
        val totalImages = imageSyncStatus.nonNegativeProgress.let { (_, total) -> total }
        val totalEvents = eventSyncState.nonNegativeProgress.let { (_, total) -> total }

        return when {
            // Combined progressbar in pre-logout screen, event sync done => updating images part in [0.5;1] range
            isPreLogoutUpSync && eventSyncState.isSyncCompleted() && totalImages > 0 -> (0.5f + 0.5f * imagePartProgressProportion)

            // Combined progressbar in pre-logout screen, event sync in progress => updating events part in [0;0.5] range
            isPreLogoutUpSync && eventSyncState.isSyncInProgress() && totalEvents > 0 -> 0.5f * eventPartProgressProportion

            // Showing only event sync progress
            eventSyncState.isSyncInProgress() && totalEvents > 0 -> eventPartProgressProportion

            // Sync hasn't started
            eventSyncState.isSyncConnecting() || !eventSyncState.hasSyncHistory() -> 0f

            // Sync done
            else -> 1f
        }
    }

    private val EventSyncState.lastSyncTimestamp: Timestamp
        get() = lastSyncTime ?: Timestamp(-1)

    private fun getSyncVisibleState(
        isSyncInProgress: Boolean,
        isOnline: Boolean,
        isPreLogoutUpSync: Boolean,
        projectConfig: ProjectConfiguration,
        moduleCounts: List<ModuleCount>,
        eventSyncState: EventSyncState,
    ): RecordSyncVisibleState {
        val isModuleSelectionRequired =
            !isPreLogoutUpSync && projectConfig.isModuleSelectionAvailable() && moduleCounts.isEmpty()

        val isCommCareSyncExpected =
            !isPreLogoutUpSync && projectConfig.isCommCareEventDownSyncAllowed()
        val isCommCareSyncBlockedByDeniedPermission =
            isCommCareSyncExpected &&
                eventSyncState.isSyncFailedBecauseCommCarePermissionIsMissing() &&
                !commCarePermissionChecker.hasCommCarePermissions()
        val isEventSyncConnectionBlocked =
            !isOnline && !isCommCareSyncExpected // CommCare would be able to sync even if device is offline
        val isSyncFailedForNonCommCareReason =
            eventSyncState.isSyncFailed() && !eventSyncState.isSyncFailedBecauseCommCarePermissionIsMissing()

        return when {
            isSyncInProgress -> RecordSyncVisibleState.IN_PROGRESS
            isCommCareSyncBlockedByDeniedPermission -> RecordSyncVisibleState.COMM_CARE_ERROR
            isModuleSelectionRequired -> RecordSyncVisibleState.NO_MODULES_ERROR
            isEventSyncConnectionBlocked -> RecordSyncVisibleState.OFFLINE_ERROR
            isSyncFailedForNonCommCareReason -> RecordSyncVisibleState.ERROR
            isPreLogoutUpSync -> RecordSyncVisibleState.NOTHING
            else -> RecordSyncVisibleState.ON_STANDBY
        }
    }

    private fun isSyncButtonEnabled(
        isOnline: Boolean,
        isPreLogoutUpSync: Boolean,
        projectConfig: ProjectConfiguration,
        eventSyncState: EventSyncState,
        recordSyncVisibleState: RecordSyncVisibleState,
    ): Boolean {
        val isReLoginRequired = eventSyncState.isSyncFailedBecauseReloginRequired()
        val isEventUpSyncPossible = isOnline && projectConfig.canSyncDataToSimprints()
        val isDownSyncPossible =
            (isOnline && !isReLoginRequired && projectConfig.isSimprintsEventDownSyncAllowed()) ||
                (
                    projectConfig.isCommCareEventDownSyncAllowed() &&
                        (
                            !eventSyncState.isSyncFailedBecauseCommCarePermissionIsMissing() ||
                                commCarePermissionChecker.hasCommCarePermissions()
                        )
                )
        return ((!isPreLogoutUpSync && isDownSyncPossible) || isEventUpSyncPossible) &&
            (recordSyncVisibleState == RecordSyncVisibleState.ON_STANDBY || recordSyncVisibleState == RecordSyncVisibleState.ERROR)
    }
}
