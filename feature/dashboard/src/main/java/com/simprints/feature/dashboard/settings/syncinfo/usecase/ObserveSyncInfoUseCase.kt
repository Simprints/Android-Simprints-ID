package com.simprints.feature.dashboard.settings.syncinfo.usecase

import com.simprints.core.DispatcherBG
import com.simprints.core.lifecycle.AppForegroundStateTracker
import com.simprints.core.tools.extentions.onChange
import com.simprints.core.tools.time.Ticker
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.RecordSyncVisibleState
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfo
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoError
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgress
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgressPart
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionImages
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionModules
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionRecords
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSampleUploadEnabledInProject
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.eventsync.permission.CommCarePermissionChecker
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.usecase.ObserveSyncableCountsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

internal class ObserveSyncInfoUseCase @Inject constructor(
    private val connectivityTracker: ConnectivityTracker,
    private val authStore: AuthStore,
    private val timeHelper: TimeHelper,
    private val ticker: Ticker,
    private val appForegroundStateTracker: AppForegroundStateTracker,
    private val commCarePermissionChecker: CommCarePermissionChecker,
    private val observeConfigurationFlow: ObserveConfigurationChangesUseCase,
    private val observeSyncableCounts: ObserveSyncableCountsUseCase,
    private val syncOrchestrator: SyncOrchestrator,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
) {
    // Since we are not using distinctUntilChanged any emission from combined flows will trigger the main flow as well
    private fun combinedRefreshSignals() = combine(
        connectivityTracker.observeIsConnected(),
        appForegroundStateTracker.observeAppInForeground().filter { it }, // only when going to foreground
        ticker.observeTicks(1.minutes),
    ) { isOnline, _, _ -> isOnline }

    operator fun invoke(isPreLogoutUpSync: Boolean = false): Flow<SyncInfo> = combine(
        combinedRefreshSignals(),
        authStore.observeSignedInProjectId(),
        syncOrchestrator.observeSyncState(),
        observeSyncableCounts(),
        observeConfigurationFlow(),
    ) { isOnline, projectId, (eventSyncState, imageSyncStatus), counts, (isRefreshing, isProjectRunning, moduleCounts, projectConfig) ->
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
        val imagesNormalizedProgress = if (imageSyncStatus.isSyncing && totalImages > 0) imageProgressProportion else 1f

        val imagesToUpload = if (imageSyncStatus.isSyncing) {
            null
        } else {
            counts.samplesToUpload // internal term is sample, user-facing (within sync info) term is image
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
        val imageSyncProgress = if (imageSyncStatus.isSyncing) {
            SyncInfoProgress(
                progressParts = listOf(imageSyncProgressPart),
                progressBarPercentage = (imagesNormalizedProgress * 100).roundToInt(),
            )
        } else {
            SyncInfoProgress()
        }

        val eventLastSyncTimestamp = eventSyncState.lastSyncTime ?: Timestamp(-1)
        val imageLastSyncTimestamp = Timestamp(imageSyncStatus.lastUpdateTimeMillis ?: -1)

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
            else -> counts.totalRecords
        }
        val recordsToUpload = when {
            isEventSyncInProgress -> null
            else -> counts.enrolmentsToUpload
        }
        val recordsToDownload = when {
            isEventSyncInProgress || isPreLogoutUpSync -> null
            projectConfig.isSimprintsEventDownSyncAllowed() -> with(counts) {
                DownSyncCounts(recordEventsToDownload, isRecordEventsToDownloadLowerBound)
            }
            else -> DownSyncCounts(0, isLowerBound = false)
        }?.let { "${it.count}${if (it.isLowerBound) "+" else ""}" }.orEmpty()

        val syncInfoSectionModules = SyncInfoSectionModules(
            isSectionAvailable = projectConfig.isModuleSelectionAvailable(),
            moduleCounts = moduleCounts.prependTotalModuleCount(),
        )

        val syncInfoSectionRecords = SyncInfoSectionRecords(
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

        val syncInfoSectionImages = SyncInfoSectionImages(
            counterImagesToUpload = imagesToUpload?.toString().orEmpty(),
            isInstructionDefaultVisible = !imageSyncStatus.isSyncing && isOnline,
            isInstructionOfflineVisible = !isOnline,
            isProgressVisible = imageSyncStatus.isSyncing,
            progress = imageSyncProgress,
            isSyncButtonEnabled = isOnline && !isReLoginRequired,
            isFooterLastSyncTimeVisible = !imageSyncStatus.isSyncing && imageLastSyncTimestamp.ms >= 0,
            footerLastSyncMinutesAgo = timeHelper.readableBetweenNowAndTime(imageLastSyncTimestamp),
        )

        val syncInfo = SyncInfo(
            isLoggedIn = projectId.isNotEmpty(),
            isConfigurationLoadingProgressBarVisible = isRefreshing,
            isLoginPromptSectionVisible = isReLoginRequired && !isPreLogoutUpSync,
            isImageSyncSectionVisible = projectConfig.isSampleUploadEnabledInProject(),
            syncInfoSectionRecords = syncInfoSectionRecords,
            syncInfoSectionImages = syncInfoSectionImages,
            syncInfoSectionModules = syncInfoSectionModules,
        )
        return@combine syncInfo
    }.onRecordSyncComplete { delay(timeMillis = SYNC_COMPLETION_HOLD_MILLIS) }
        .onImageSyncComplete { delay(timeMillis = SYNC_COMPLETION_HOLD_MILLIS) }
        .flowOn(dispatcher) // upstream flows do a lot of computation

    private fun calculateProportion(
        current: Int,
        total: Int,
    ): Float = if (total == 0) 0f else (current.toFloat() / total).coerceIn(0f, 1f)

    private fun List<ModuleCount>.prependTotalModuleCount(): List<ModuleCount> = if (isEmpty()) {
        emptyList()
    } else {
        listOf(ModuleCount(name = "", count = sumOf { it.count })) + this
    }

    // sync info change detection helpers

    private fun Flow<SyncInfo>.onRecordSyncComplete(action: suspend (SyncInfo) -> Unit) = onChange(
        comparator = { previous, current ->
            previous.syncInfoSectionRecords.isProgressVisible && !current.syncInfoSectionRecords.isProgressVisible
        },
        action,
    )

    private fun Flow<SyncInfo>.onImageSyncComplete(action: suspend (SyncInfo) -> Unit) = onChange(
        comparator = { previous, current ->
            previous.syncInfoSectionImages.isProgressVisible && !current.syncInfoSectionImages.isProgressVisible
        },
        action,
    )

    private companion object {
        private const val SYNC_COMPLETION_HOLD_MILLIS = 1000L
    }
}
