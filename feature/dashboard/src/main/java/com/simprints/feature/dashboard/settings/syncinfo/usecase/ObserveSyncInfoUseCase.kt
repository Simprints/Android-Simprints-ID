package com.simprints.feature.dashboard.settings.syncinfo.usecase

import androidx.lifecycle.asFlow
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.lifecycle.AppForegroundStateTracker
import com.simprints.core.tools.extentions.combine9
import com.simprints.core.tools.extentions.onChange
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Ticker
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfo
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoError
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoModuleCount
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgress
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoProgressPart
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionImages
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionModules
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionRecords
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.store.models.isCommCareEventDownSyncAllowed
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSimprintsEventDownSyncAllowed
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.network.ConnectivityTracker
import com.simprints.infra.sync.SyncOrchestrator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.math.roundToInt

internal class ObserveSyncInfoUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val connectivityTracker: ConnectivityTracker,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val authStore: AuthStore,
    private val imageRepository: ImageRepository,
    private val eventSyncManager: EventSyncManager,
    syncOrchestrator: SyncOrchestrator,
    private val tokenizationProcessor: TokenizationProcessor,
    private val timeHelper: TimeHelper,
    private val ticker: Ticker,
    private val appForegroundStateTracker: AppForegroundStateTracker,
) {
    private val eventSyncStateFlow =
        eventSyncManager.getLastSyncState(useDefaultValue = true /* otherwise value not guaranteed */).asFlow()
    private val imageSyncStatusFlow =
        syncOrchestrator.observeImageSyncStatus()

    operator fun invoke(isPreLogoutUpSync: Boolean = false): Flow<SyncInfo> = combine9(
        connectivityTracker.observeIsConnected().asFlow(),
        authStore.observeSignedInProjectId().map(String::isNotEmpty),
        configManager.observeIsProjectRefreshing(),
        eventSyncStateFlow,
        imageSyncStatusFlow,
        configManager.observeProjectConfiguration(),
        configManager.observeDeviceConfiguration(),
        appForegroundStateTracker.observeAppInForeground().filter { it }, // only when going to foreground
        ticker.observeTickOncePerMinute(),
    ) { isOnline, isLoggedIn, isRefreshing, eventSyncState, imageSyncStatus, projectConfig, deviceConfig, _, _ ->
        val currentEvents = eventSyncState.progress?.coerceAtLeast(0) ?: 0
        val totalEvents = eventSyncState.total?.takeIf { it >= 1 } ?: 0
        val currentImages = imageSyncStatus.progress?.first?.coerceAtLeast(0) ?: 0
        val totalImages = imageSyncStatus.progress?.second?.takeIf { it >= 1 } ?: 0

        val eventsNormalizedProgress = when {
            isPreLogoutUpSync && eventSyncState.isSyncCompleted() && totalImages > 0 ->
                (0.5f + 0.5f * currentImages / totalImages).coerceIn(0.5f, 1f) // combined progress 2nd half - images

            isPreLogoutUpSync && eventSyncState.isSyncInProgress() && totalEvents > 0 ->
                (0.5f * currentEvents / totalEvents).coerceIn(0f, 0.5f) // combined progress 1st half - events

            eventSyncState.isSyncInProgress() && totalEvents > 0 ->
                (currentEvents.toFloat() / totalEvents).coerceIn(0f, 1f)

            eventSyncState.isSyncConnecting() || eventSyncState.isThereNotSyncHistory() -> 0f
            else -> 1f
        }
        val imagesNormalizedProgress = when {
            imageSyncStatus.isSyncing && totalImages > 0 ->
                (currentImages.toFloat() / totalImages).coerceIn(0f, 1f)

            else -> 1f
        }

        val imagesToUpload =
            if (imageSyncStatus.isSyncing) {
                null
            } else {
                imageRepository.getNumberOfImagesToUpload(projectId = authStore.signedInProjectId)
            }

        val eventSyncProgressPart = SyncInfoProgressPart(
            isPending = eventSyncState.isSyncConnecting() || eventSyncState.isThereNotSyncHistory(),
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

        val isEventSyncInProgress =
            eventSyncState.isSyncInProgress() ||
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

        val eventLastSyncTimestamp = eventSyncManager.getLastSyncTime() ?: Timestamp(-1)
        val imageLastSyncTimestamp = imageSyncStatus.lastUpdateTimeMillis?.let {
            Timestamp(it)
        } ?: Timestamp(-1)

        val isReLoginRequired = eventSyncState.isSyncFailedBecauseReloginRequired()

        val isModuleSelectionRequired =
            !isPreLogoutUpSync && projectConfig.isModuleSelectionAvailable() && deviceConfig.selectedModules.isEmpty()

        val isCommCareSyncExpected =
            !isPreLogoutUpSync && projectConfig.isCommCareEventDownSyncAllowed()

        val isCommCareSyncBlockedByDeniedPermission =
            isCommCareSyncExpected && eventSyncState.isSyncFailedBecauseCommCarePermissionIsMissing()
        val isEventSyncConnectionBlocked =
            !isOnline && !isCommCareSyncExpected // CommCare would be able to sync even if device is offline

        // an intermediate calculation of sync state shown in UI - not to be confused with the data layer-specific EventSyncState
        val eventSyncVisibleState = when {
            isEventSyncInProgress -> InProgress
            isCommCareSyncBlockedByDeniedPermission -> CommCareError
            isModuleSelectionRequired -> NoModulesError
            isEventSyncConnectionBlocked -> OfflineError
            eventSyncState.isSyncFailed() -> Error
            else -> OnStandby
        }

        val isEventUpSyncPossible =
            projectConfig.canSyncDataToSimprints() && isOnline
        val isDownSyncPossible =
            (projectConfig.isSimprintsEventDownSyncAllowed() && isOnline && !isReLoginRequired) ||
                (projectConfig.isCommCareEventDownSyncAllowed() && !eventSyncState.isSyncFailedBecauseCommCarePermissionIsMissing())
        val isSyncButtonEnabled =
            (eventSyncVisibleState == OnStandby) &&
                ((!isPreLogoutUpSync && isDownSyncPossible) || isEventUpSyncPossible)

        val projectId = authStore.signedInProjectId

        val recordsTotal = when {
            isEventSyncInProgress -> null
            else -> enrolmentRecordRepository.count(SubjectQuery(projectId))
        }
        val recordsToUpload = when {
            isEventSyncInProgress -> null
            else ->
                eventSyncManager
                    .countEventsToUpload(
                        listOf(EventType.ENROLMENT_V2, EventType.ENROLMENT_V4),
                    ).firstOrNull() ?: 0
        }
        val recordsToDownload = when {
            isEventSyncInProgress -> null
            isPreLogoutUpSync -> null
            projectConfig.isSimprintsEventDownSyncAllowed() -> try {
                withTimeout(COUNT_EVENTS_TIMEOUT_MILLIS) {
                    countEventsToDownloadWithCaching()
                }
            } catch (_: Throwable) {
                DownSyncCounts(0, isLowerBound = false)
            }

            else -> DownSyncCounts(0, isLowerBound = false)
        }

        val project = configManager.getProject(projectId)
        val isProjectEnding =
            project.state == ProjectState.PROJECT_ENDING
        val moduleCounts = deviceConfig.selectedModules.map { moduleName ->
            ModuleCount(
                name = when (moduleName) {
                    is TokenizableString.Raw -> moduleName
                    is TokenizableString.Tokenized -> tokenizationProcessor.decrypt(
                        encrypted = moduleName,
                        tokenKeyType = TokenKeyType.ModuleId,
                        project,
                    )
                }.value,
                count = enrolmentRecordRepository.count(
                    SubjectQuery(projectId = projectId, moduleId = moduleName),
                ),
            )
        }
        val modulesCountTotal = SyncInfoModuleCount(
            isTotal = true,
            name = "",
            count = moduleCounts.sumOf { it.count }.toString(),
        )
        val syncInfoSectionModules = SyncInfoSectionModules(
            isSectionAvailable = projectConfig.isModuleSelectionAvailable(),
            moduleCounts = listOfNotNull(
                modulesCountTotal.takeIf { moduleCounts.isNotEmpty() },
            ) + moduleCounts.map { moduleCount ->
                SyncInfoModuleCount(
                    isTotal = false,
                    name = moduleCount.name,
                    count = moduleCount.count.toString(),
                )
            },
        )

        val syncInfoSectionRecords = SyncInfoSectionRecords(
            counterTotalRecords = recordsTotal?.toString().orEmpty(),
            counterRecordsToUpload = recordsToUpload?.toString().orEmpty(),
            isCounterRecordsToDownloadVisible = !isPreLogoutUpSync && !isProjectEnding,
            counterRecordsToDownload = recordsToDownload?.let { "${it.count}${if (it.isLowerBound) "+" else ""}" }.orEmpty(),
            isCounterImagesToUploadVisible = isPreLogoutUpSync,
            counterImagesToUpload = imagesToUpload?.toString().orEmpty(),
            isInstructionDefaultVisible = eventSyncVisibleState == OnStandby,
            isInstructionCommCarePermissionVisible = eventSyncVisibleState == CommCareError,
            isInstructionNoModulesVisible = eventSyncVisibleState == NoModulesError,
            isInstructionOfflineVisible = eventSyncVisibleState == OfflineError,
            isInstructionErrorVisible = eventSyncVisibleState == Error,
            instructionPopupErrorInfo = SyncInfoError(
                isBackendMaintenance = eventSyncState.isSyncFailedBecauseBackendMaintenance(),
                backendMaintenanceEstimatedOutage = eventSyncState.getEstimatedBackendMaintenanceOutage() ?: -1,
                isTooManyRequests = eventSyncState.isSyncFailedBecauseTooManyRequests(),
            ),
            isProgressVisible = eventSyncVisibleState == InProgress,
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
            isLoggedIn,
            isConfigurationLoadingProgressBarVisible = isRefreshing,
            isLoginPromptSectionVisible = isReLoginRequired && !isPreLogoutUpSync,
            syncInfoSectionRecords,
            syncInfoSectionImages,
            syncInfoSectionModules,
        )
        return@combine9 syncInfo
    }.onRecordSyncComplete {
        delay(timeMillis = SYNC_COMPLETION_HOLD_MILLIS)
    }.onImageSyncComplete {
        delay(timeMillis = SYNC_COMPLETION_HOLD_MILLIS)
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

    // caching eventSyncManager.countEventsToDownload to avoid network-based delays on frequent calls

    private var cachedEventCountToDownload: DownSyncCounts? = null
    private var cachedEventCountToDownloadTimestamp: Long = 0

    private suspend fun countEventsToDownloadWithCaching(): DownSyncCounts {
        val timeNowMs = timeHelper.now().ms
        cachedEventCountToDownload
            ?.takeIf {
                timeNowMs - cachedEventCountToDownloadTimestamp < COUNT_EVENTS_CACHE_LIFESPAN_MILLIS
            }?.let {
                return it
            }
        cachedEventCountToDownloadTimestamp = timeNowMs
        return eventSyncManager.countEventsToDownload().also {
            cachedEventCountToDownload = it
        }
    }

    private companion object {
        private const val SYNC_COMPLETION_HOLD_MILLIS = 1000L
        private const val COUNT_EVENTS_TIMEOUT_MILLIS = 10 * 1000L
        private const val COUNT_EVENTS_CACHE_LIFESPAN_MILLIS = 10 * 1000L
    }
}

/**
 * A representation of a non-overlapping, exhaustive "sync state" as shown in UI.
 * To be used in a temporary UI state calculation: good to be used with exhaustive pattern matching.
 * Not to be confused with the data layer-specific EventSyncState.
  */
private sealed class EventSyncVisibleState

private object OnStandby : EventSyncVisibleState()

private object InProgress : EventSyncVisibleState()

private object CommCareError : EventSyncVisibleState()

private object NoModulesError : EventSyncVisibleState()

private object OfflineError : EventSyncVisibleState()

private object Error : EventSyncVisibleState()
