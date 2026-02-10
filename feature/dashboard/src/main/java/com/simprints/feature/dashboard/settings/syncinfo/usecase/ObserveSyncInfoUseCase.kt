package com.simprints.feature.dashboard.settings.syncinfo.usecase

import com.simprints.core.DispatcherBG
import com.simprints.core.lifecycle.AppForegroundStateTracker
import com.simprints.core.tools.extentions.onChange
import com.simprints.core.tools.time.Ticker
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfo
import com.simprints.feature.dashboard.settings.syncinfo.SyncInfoSectionModules
import com.simprints.feature.dashboard.settings.syncinfo.modulecount.ModuleCount
import com.simprints.feature.dashboard.settings.syncinfo.usecase.internal.GetSyncInfoSectionImagesUseCase
import com.simprints.feature.dashboard.settings.syncinfo.usecase.internal.GetSyncInfoSectionRecordsUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.store.models.isSampleUploadEnabledInProject
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
import kotlin.time.Duration.Companion.minutes

internal class ObserveSyncInfoUseCase @Inject constructor(
    private val connectivityTracker: ConnectivityTracker,
    private val authStore: AuthStore,
    private val ticker: Ticker,
    private val appForegroundStateTracker: AppForegroundStateTracker,
    private val getSyncInfoSectionImages: GetSyncInfoSectionImagesUseCase,
    private val getSyncInfoSectionRecords: GetSyncInfoSectionRecordsUseCase,
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
    ) {
        isOnline,
        projectId,
        (eventSyncState, imageSyncStatus),
        syncableCounts,
        (isRefreshing, isProjectRunning, moduleCounts, projectConfig),
        ->
        val isReLoginRequired = eventSyncState.isSyncFailedBecauseReloginRequired()
        val syncInfoSectionModules = SyncInfoSectionModules(
            isSectionAvailable = projectConfig.isModuleSelectionAvailable(),
            moduleCounts = moduleCounts.prependTotalModuleCount(),
        )
        val syncInfoSectionRecords = getSyncInfoSectionRecords(
            isPreLogoutUpSync,
            isOnline,
            projectId,
            eventSyncState,
            imageSyncStatus,
            syncableCounts,
            isProjectRunning,
            moduleCounts,
            projectConfig,
        )
        val syncInfoSectionImages = getSyncInfoSectionImages(
            isOnline,
            eventSyncState,
            imageSyncStatus,
            syncableCounts,
        )
        return@combine SyncInfo(
            isLoggedIn = projectId.isNotEmpty(),
            isConfigurationLoadingProgressBarVisible = isRefreshing,
            isLoginPromptSectionVisible = isReLoginRequired && !isPreLogoutUpSync,
            isImageSyncSectionVisible = projectConfig.isSampleUploadEnabledInProject(),
            syncInfoSectionRecords,
            syncInfoSectionImages,
            syncInfoSectionModules,
        )
    }.onRecordSyncComplete { delay(timeMillis = SYNC_COMPLETION_HOLD_MILLIS) }
        .onImageSyncComplete { delay(timeMillis = SYNC_COMPLETION_HOLD_MILLIS) }
        .flowOn(dispatcher) // upstream flows do a lot of computation

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
