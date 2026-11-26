package com.simprints.feature.dashboard.settings.syncinfo

import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.dashboard.settings.syncinfo.usecase.ObserveSyncInfoUseCase
import com.simprints.feature.login.LoginParams
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.OneTime
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class SyncInfoViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val authStore: AuthStore,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val timeHelper: TimeHelper,
    observeSyncInfo: ObserveSyncInfoUseCase,
    private val syncOrchestrator: SyncOrchestrator,
    private val deviceStateDataTracker: DeviceStateDataTracker,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : BaseMviViewModel<SyncInfoUiState, SyncInfoUiAction, SyncInfoUiEffect>(SyncInfoUiState()) {
    // Reducer-owned part of the state (host mode); the sync info stream is combined into uiState below.
    private val reducerState: StateFlow<SyncInfoUiState> = super.uiState

    // Pre-logout mode as attached by the host. Nothing that depends on it runs before it is known.
    private val preLogoutMode: Flow<Boolean> = reducerState.mapNotNull { it.isPreLogoutUpSync }.distinctUntilChanged()

    private val syncStatusFlow = syncOrchestrator.observeSyncState()
    private val eventSyncStateFlow =
        syncStatusFlow.map { it.eventSyncState }
    private val imageSyncStatusFlow =
        syncStatusFlow.map { it.imageSyncStatus }

    // Set by the first start of the sync info stream; later starts are restarts.
    private var hasStreamStarted = false

    private val dataLayerDrivenSyncInfoFlow = preLogoutMode
        .flatMapLatest { isPreLogoutUpSync -> observeSyncInfo(isPreLogoutUpSync) }
        .onStart {
            // The initial-sync decision runs on every start of this stream (after 5 s or more without a collector).
            // The pre-logout image auto-start is not here: it runs once for the ViewModel's lifetime, from init.
            val isFirstStreamStart = !hasStreamStarted
            hasStreamStarted = true
            launchWork { startInitialSyncIfRequired(isFirstStreamStart) }
        }.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1,
        )

    /*
     * Visual sync button responsiveness optimization
     *
     * * The problem: data layer-driven progress visualization is simple programmatically, but can be slow in the UI.
     *
     * How it would work without the optimization:
     * The forceEventSync and toggleImageSync invoke sync purely on the data layer,
     * so the UI may remain unaware of the forced sync command until data-driven evidence of sync starts appearing.
     * This may take seconds on slow devices.
     *
     * Each forced sync, invoked by the event sync and image sync clicks, immediately reshapes the flow of events:
     * At first we immediately emit a sync state that is forcefully marked as in progress, for events or images separately.
     * And we start ignoring sync states that happen before the true progress in data layer appears.
     * Once the true progress in data layer starts, we keep showing that true progress.
     * Additionally, initial progress is emitted on start, before any forced sync invocations. To prevent getting stuck.
     */

    private val eventSyncButtonClickFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val imageSyncButtonClickFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val eventSyncButtonResponsiveSyncInfo = eventSyncButtonClickFlow.flatMapLatest {
        dataLayerDrivenSyncInfoFlow
            .dropWhile { syncInfo ->
                !syncInfo.syncInfoSectionRecords.isProgressVisible
            }.onStart {
                emit(syncInfoState.value.forceEventSyncProgress())
            }
    }

    private val imageSyncButtonResponsiveSyncInfo = imageSyncButtonClickFlow.flatMapLatest {
        dataLayerDrivenSyncInfoFlow
            .dropWhile { syncInfo ->
                !syncInfo.syncInfoSectionImages.isProgressVisible
            }.onStart {
                emit(syncInfoState.value.forceImageSyncProgress())
            }
    }

    // Internal hop without a stop timeout (as the former shareIn); the 5 s stop timeout is on uiState only.
    private val syncInfoState: StateFlow<SyncInfo> = merge(
        dataLayerDrivenSyncInfoFlow,
        eventSyncButtonResponsiveSyncInfo,
        imageSyncButtonResponsiveSyncInfo,
    ).distinctUntilChanged()
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SyncInfo())

    override val uiState: StateFlow<SyncInfoUiState> = combine(reducerState, syncInfoState) { local, syncInfo ->
        local.copy(syncInfo = syncInfo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SyncInfoUiState())

    init {
        launchWork { observeAutoLogout() }
        launchWork { syncImagesAfterEventsWhenRequired() }
    }

    override suspend fun handleAction(action: SyncInfoUiAction) {
        when (action) {
            is SyncInfoUiAction.HostAttached -> reduce { state ->
                if (state.isPreLogoutUpSync == null) state.copy(isPreLogoutUpSync = action.isPreLogoutUpSync) else state
            }

            SyncInfoUiAction.SyncRecordsClicked -> forceEventSync(canEmitSyncButtonClick = true)
            SyncInfoUiAction.SyncImagesClicked -> toggleImageSync()
            SyncInfoUiAction.RequestLogin -> requestLogin()
            is SyncInfoUiAction.LoginResultReceived -> if (action.result.isSuccess) forceEventSync(canEmitSyncButtonClick = true)
            is SyncInfoUiAction.ModuleSelectionResultReceived -> if (action.result.isConfirmed) {
                forceEventSync(canEmitSyncButtonClick = false)
            }
        }
    }

    private suspend fun forceEventSync(
        canEmitSyncButtonClick: Boolean,
        isPreLogoutUpSync: Boolean = reducerState.value.isPreLogoutUpSync == true,
    ) {
        // Sync is the user's "give me fresh data" gesture, so it also recovers any failed device state source
        deviceStateDataTracker.refresh()
        if (canEmitSyncButtonClick) {
            val isEventSyncing = eventSyncStateFlow.firstOrNull()?.isSyncInProgress() == true
            if (!isEventSyncing) {
                eventSyncButtonClickFlow.emit(Unit)
            }
        }

        val isDownSyncAllowed = !isPreLogoutUpSync && configRepository.getProject()?.state == ProjectState.RUNNING
        syncOrchestrator.execute(OneTime.Events.restart(isDownSyncAllowed))
    }

    private suspend fun toggleImageSync() {
        val isImageSyncing = imageSyncStatusFlow.firstOrNull()?.isSyncing == true
        if (isImageSyncing) {
            syncOrchestrator.execute(OneTime.Images.stop())
        } else {
            imageSyncButtonClickFlow.emit(Unit)
            syncOrchestrator.execute(OneTime.Images.start())
        }
    }

    private fun performLogout() {
        syncOrchestrator.execute(OneTime.Logout.start())
    }

    private suspend fun requestLogin() {
        val params = LoginParams(
            projectId = authStore.signedInProjectId,
            userId = authStore.signedInUserId ?: recentUserActivityManager.getRecentUserActivity().lastUserUsed,
        )
        emitEffect(SyncInfoUiEffect.NavigateToLogin(params))
    }

    private suspend fun awaitPreLogoutMode(): Boolean = preLogoutMode.first()

    // initial actions

    private suspend fun startInitialSyncIfRequired(isFirstStreamStart: Boolean) {
        val isPreLogoutUpSync = awaitPreLogoutMode()
        val eventSyncState = eventSyncStateFlow
            .dropWhile { it.isUninitialized() }
            .firstOrNull()
        val isRunning = eventSyncState?.isSyncRunning() ?: false
        val isCompleted = eventSyncState?.isSyncCompleted() ?: false
        val lastUpdate = eventSyncState?.lastSyncTime

        val isForceEventSync = when {
            // Pre-logout: always on the first start; on a restart only if the sync is neither running nor already completed.
            isPreLogoutUpSync -> isFirstStreamStart || (!isRunning && !isCompleted)
            configRepository.isModuleSelectionRequired() -> false
            isRunning -> false
            lastUpdate == null -> true
            timeHelper.msBetweenNowAndTime(lastUpdate) > RE_SYNC_TIMEOUT_MILLIS -> true
            else -> false
        }
        if (isForceEventSync) {
            forceEventSync(canEmitSyncButtonClick = false, isPreLogoutUpSync = isPreLogoutUpSync)
        }
    }

    private suspend fun syncImagesAfterEventsWhenRequired() {
        if (awaitPreLogoutMode()) {
            eventSyncStateFlow
                .map { it.isSyncCompleted() }
                .distinctUntilChanged()
                .collect { isEventSyncCompleted ->
                    if (isEventSyncCompleted) {
                        syncOrchestrator.execute(OneTime.Images.start())
                    }
                }
        }
    }

    private suspend fun observeAutoLogout() {
        // Outside pre-logout there is never a logout, so the device data counts are not observed at all.
        if (!awaitPreLogoutMode()) return
        // TODO MS-1761: move this into a pre-logout sync use case
        syncStatusFlow
            .combine(deviceStateDataTracker.observeDeviceDataState()) { (eventSyncState, imageSyncStatus), deviceDataState ->
                eventSyncState.isSyncCompleted() && !imageSyncStatus.isSyncing && !deviceDataState.hasDataToUpload
            }.debounce(LOGOUT_DELAY_MILLIS)
            .filter { isReadyToLogOut -> isReadyToLogOut }
            .onEach { performLogout() }
            .flowOn(ioDispatcher)
            .collect { }
    }

    private fun SyncInfo.forceEventSyncProgress() = copy(
        syncInfoSectionRecords = syncInfoSectionRecords.copy(
            counterTotalRecords = "",
            counterRecordsToUpload = "",
            counterRecordsToDownload = "",
            recordSyncVisibleState = RecordSyncVisibleState.NOTHING,
            isProgressVisible = true,
            isSyncButtonEnabled = false,
            footerLastSyncMinutesAgo = "",
        ),
    )

    private fun SyncInfo.forceImageSyncProgress() = copy(
        syncInfoSectionImages = syncInfoSectionImages.copy(
            counterImagesToUpload = "",
            isInstructionDefaultVisible = false,
            isInstructionOfflineVisible = false,
            isProgressVisible = true,
            footerLastSyncMinutesAgo = "",
        ),
    )

    private suspend fun ConfigRepository.isModuleSelectionRequired() =
        getProjectConfiguration().isModuleSelectionAvailable() && getDeviceConfiguration().selectedModules.isEmpty()

    private companion object {
        private const val RE_SYNC_TIMEOUT_MILLIS = 5 * 60 * 1000L
        private const val LOGOUT_DELAY_MILLIS = 3000L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
