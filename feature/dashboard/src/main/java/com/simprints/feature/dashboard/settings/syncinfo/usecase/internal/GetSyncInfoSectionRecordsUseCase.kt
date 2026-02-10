package com.simprints.feature.dashboard.settings.syncinfo.usecase.internal

import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.RecordSyncVisibleState
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoError
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgress
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgressPart
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionRecords
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
        val currentEvents = eventSyncState.progress?.coerceAtLeast(0) ?: 0
        val totalEvents = eventSyncState.total?.coerceAtLeast(0) ?: 0
        val currentImages = imageSyncStatus.progress?.first?.coerceAtLeast(0) ?: 0
        val totalImages = imageSyncStatus.progress?.second?.takeIf { it >= 1 } ?: 0

        val eventProgressProportion = calculateProportion(currentEvents, totalEvents)
        val imageProgressProportion = calculateProportion(currentImages, totalImages)

        val eventsNormalizedProgress = when {
            // Combined progressbar in pre-logout screen, event sync done => updating images part in [0.5;1] range
            isPreLogoutUpSync && eventSyncState.isSyncCompleted() && totalImages > 0 -> (0.5f + imageProgressProportion / 2)

            // Combined progressbar in pre-logout screen, event sync in progress => updating events part in [0;0.5] range
            isPreLogoutUpSync && eventSyncState.isSyncInProgress() && totalEvents > 0 -> eventProgressProportion / 2

            // Showing only event sync progress
            eventSyncState.isSyncInProgress() && totalEvents > 0 -> eventProgressProportion

            // Sync hasn't started
            eventSyncState.isSyncConnecting() || !eventSyncState.hasSyncHistory() -> 0f

            // Sync done
            else -> 1f
        }

        val imagesToUpload = if (imageSyncStatus.isSyncing) {
            null
        } else {
            syncableCounts.samplesToUpload // internal term is sample, user-facing (within sync info) term is image
        }

        val eventSyncProgressPart = SyncInfoProgressPart(
            isPending = eventSyncState.isSyncConnecting() || !eventSyncState.hasSyncHistory(),
            isDone = eventSyncState.isSyncCompleted(),
            areNumbersVisible = eventSyncState.isSyncInProgress() && totalEvents > 0,
            currentNumber = currentEvents,
            totalNumber = totalEvents,
        )
        val imageSyncProgressPart = SyncInfoProgressPart(
            isPending = eventSyncState.isSyncInProgress() && !imageSyncStatus.isSyncing,
            isDone = !eventSyncState.isSyncInProgress() && !imageSyncStatus.isSyncing && imagesToUpload == 0,
            areNumbersVisible = imageSyncStatus.isSyncing && totalImages > 0,
            currentNumber = currentImages,
            totalNumber = totalImages,
        )

        val isEventSyncInProgress = eventSyncState.isSyncInProgress() ||
            (isPreLogoutUpSync && imageSyncStatus.isSyncing) // if combined with images
        val eventSyncProgress = if (isEventSyncInProgress) {
            SyncInfoProgress(
                progressParts = if (isPreLogoutUpSync) {
                    listOf(eventSyncProgressPart, imageSyncProgressPart)
                } else {
                    listOf(eventSyncProgressPart)
                },
                progressBarPercentage = (eventsNormalizedProgress * 100).roundToInt(),
            )
        } else {
            SyncInfoProgress()
        }

        val eventLastSyncTimestamp = eventSyncState.lastSyncTime ?: Timestamp(-1)

        val isReLoginRequired = eventSyncState.isSyncFailedBecauseReloginRequired()

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

        // an intermediate calculation of sync state shown in UI - not to be confused with the data layer-specific EventSyncState
        val recordSyncVisibleState = when {
            isEventSyncInProgress -> RecordSyncVisibleState.IN_PROGRESS
            isCommCareSyncBlockedByDeniedPermission -> RecordSyncVisibleState.COMM_CARE_ERROR
            isModuleSelectionRequired -> RecordSyncVisibleState.NO_MODULES_ERROR
            isEventSyncConnectionBlocked -> RecordSyncVisibleState.OFFLINE_ERROR
            isSyncFailedForNonCommCareReason -> RecordSyncVisibleState.ERROR
            isPreLogoutUpSync -> RecordSyncVisibleState.NOTHING
            else -> RecordSyncVisibleState.ON_STANDBY
        }

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
        val isSyncButtonEnabled = ((!isPreLogoutUpSync && isDownSyncPossible) || isEventUpSyncPossible) &&
            (recordSyncVisibleState == RecordSyncVisibleState.ON_STANDBY || recordSyncVisibleState == RecordSyncVisibleState.ERROR)

        val recordsTotal = when {
            isEventSyncInProgress || projectId.isBlank() -> null
            else -> syncableCounts.totalRecords
        }
        val recordsToUpload = when {
            isEventSyncInProgress -> null
            else -> syncableCounts.enrolmentsToUpload
        }
        val recordsToDownload = when {
            isEventSyncInProgress || isPreLogoutUpSync -> null
            projectConfig.isSimprintsEventDownSyncAllowed() -> with(syncableCounts) {
                DownSyncCounts(recordEventsToDownload, isRecordEventsToDownloadLowerBound)
            }
            else -> DownSyncCounts(0, isLowerBound = false)
        }?.let { "${it.count}${if (it.isLowerBound) "+" else ""}" }.orEmpty()

        return SyncInfoSectionRecords(
            counterTotalRecords = recordsTotal?.toString().orEmpty(),
            counterRecordsToUpload = recordsToUpload?.toString().orEmpty(),
            isCounterRecordsToDownloadVisible = !isPreLogoutUpSync && isProjectRunning,
            counterRecordsToDownload = recordsToDownload,
            isCounterImagesToUploadVisible = isPreLogoutUpSync,
            counterImagesToUpload = imagesToUpload?.toString().orEmpty(),
            recordSyncVisibleState = recordSyncVisibleState,
            instructionPopupErrorInfo = SyncInfoError(
                isBackendMaintenance = eventSyncState.isSyncFailedBecauseBackendMaintenance(),
                backendMaintenanceEstimatedOutage = eventSyncState.getEstimatedBackendMaintenanceOutage() ?: -1,
                isTooManyRequests = eventSyncState.isSyncFailedBecauseTooManyRequests(),
            ),
            isProgressVisible = recordSyncVisibleState == RecordSyncVisibleState.IN_PROGRESS,
            progress = eventSyncProgress,
            isSyncButtonVisible = !isPreLogoutUpSync || eventSyncState.isSyncFailed(),
            isSyncButtonEnabled = isSyncButtonEnabled,
            isSyncButtonForRetry = eventSyncState.isSyncFailed(),
            isFooterSyncInProgressVisible = isPreLogoutUpSync && isEventSyncInProgress,
            isFooterReadyToLogOutVisible = isPreLogoutUpSync && eventSyncState.isSyncCompleted() && !imageSyncStatus.isSyncing,
            isFooterSyncIncompleteVisible = isPreLogoutUpSync && eventSyncState.isSyncFailed(),
            isFooterLastSyncTimeVisible = !isPreLogoutUpSync && !eventSyncState.isSyncInProgress() && eventLastSyncTimestamp.ms >= 0,
            footerLastSyncMinutesAgo = timeHelper.readableBetweenNowAndTime(eventLastSyncTimestamp),
        )
    }

    private fun calculateProportion(
        current: Int,
        total: Int,
    ): Float = if (total == 0) 0f else (current.toFloat() / total).coerceIn(0f, 1f)
}
