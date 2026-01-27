package com.simprints.feature.dashboard.settings.syncinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.dashboard.settings.syncinfo.usecase.ObserveSyncInfoUseCase
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.LoginResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.usecase.SyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SyncInfoViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val authStore: AuthStore,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val timeHelper: TimeHelper,
    observeSyncInfo: ObserveSyncInfoUseCase,
    private val sync: SyncUseCase,
    private val logoutUseCase: LogoutUseCase,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    var isPreLogoutUpSync = false

    val loginNavigationEventLiveData: LiveData<LoginParams>
        get() = _loginNavigationEventLiveData
    private val _loginNavigationEventLiveData = MutableLiveData<LoginParams>()

    private val syncStatusFlow = sync(SyncCommands.ObserveOnly).syncStatusFlow
    private val eventSyncStateFlow =
        syncStatusFlow.map { it.eventSyncState }
    private val imageSyncStatusFlow =
        syncStatusFlow.map { it.imageSyncStatus }

    private val eventSyncButtonClickFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val imageSyncButtonClickFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val logoutEventFlow: Flow<LogoutActionReason?> = combine(
        authStore.observeSignedInProjectId(),
        syncStatusFlow,
    ) { projectId, (eventSyncState, imageSyncStatus) ->
        when {
            projectId.isEmpty() -> LogoutActionReason.PROJECT_ENDING_OR_DEVICE_COMPROMISED
            isPreLogoutUpSync && eventSyncState.isSyncCompleted() && !imageSyncStatus.isSyncing -> LogoutActionReason.USER_ACTION
            else -> null
        }
    }.debounce(LOGOUT_DELAY_MILLIS)
        .filter { it != null }
        .flowOn(ioDispatcher)

    val syncInfoLiveData: LiveData<SyncInfo> by lazy {
        val dataLayerDrivenSyncInfoFlow = observeSyncInfo(isPreLogoutUpSync)
            .onStart {
                startInitialSyncIfRequired()
                syncImagesAfterEventsWhenRequired()
            }.shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                replay = 1,
            )

        /*
         * Visual sync button responsiveness optimization
         *
         * The problem: data layer-driven progress visualization is simple programmatically, but can be slow in the UI.
         *
         * How it would work without the optimization:
         * The forceEventSync and toggleImageSync invoke sync purely on the data layer,
         * so the UI may remain unaware of the forced sync command until data-driven evidence of sync starts appearing.
         * This may take seconds on slow devices.
         *
         * How it works with the optimization:
         * Each forced sync, invoked by forceEventSync and toggleImageSync, immediately reshapes the flow of events:
         * At first we immediately emit a sync state that is forcefully marked as in progress, for events or images separately.
         * And we start ignoring sync states that happen before the true progress in data layer appears.
         * Once the true progress in data layer starts, we keep showing that true progress.
         * Additionally, an initial progress is emitted on start, before any forced sync invocations. To prevent getting stuck.
         */

        val eventSyncButtonResponsiveSyncInfo = eventSyncButtonClickFlow.flatMapLatest {
            dataLayerDrivenSyncInfoFlow
                .dropWhile { syncInfo ->
                    !syncInfo.syncInfoSectionRecords.isProgressVisible
                }.onStart {
                    val initialState = syncInfoLiveData.value ?: SyncInfo()
                    emit(initialState.forceEventSyncProgress())
                }
        }

        val imageSyncButtonResponsiveSyncInfo = imageSyncButtonClickFlow.flatMapLatest {
            dataLayerDrivenSyncInfoFlow
                .dropWhile { syncInfo ->
                    !syncInfo.syncInfoSectionImages.isProgressVisible
                }.onStart {
                    val initialState = syncInfoLiveData.value ?: SyncInfo()
                    emit(initialState.forceImageSyncProgress())
                }
        }

        merge(
            dataLayerDrivenSyncInfoFlow,
            eventSyncButtonResponsiveSyncInfo,
            imageSyncButtonResponsiveSyncInfo,
        ).distinctUntilChanged()
            .flowOn(ioDispatcher)
            .asLiveData(viewModelScope.coroutineContext)
    }

    fun forceEventSync(canEmitSyncButtonClick: Boolean = true) {
        viewModelScope.launch {
            if (canEmitSyncButtonClick) {
                val isEventSyncing = eventSyncStateFlow.firstOrNull()?.isSyncInProgress() == true
                if (!isEventSyncing) {
                    eventSyncButtonClickFlow.emit(Unit)
                }
            }

            val isDownSyncAllowed = !isPreLogoutUpSync && configRepository.getProject()?.state == ProjectState.RUNNING
            sync(SyncCommands.OneTime.Events.stopAndStart(isDownSyncAllowed))
        }
    }

    fun toggleImageSync() {
        viewModelScope.launch {
            val isImageSyncing = imageSyncStatusFlow.firstOrNull()?.isSyncing == true
            if (isImageSyncing) {
                sync(SyncCommands.OneTime.Images.stop())
            } else {
                imageSyncButtonClickFlow.emit(Unit)
                sync(SyncCommands.OneTime.Images.start())
            }
        }
    }

    fun performLogout() {
        logoutUseCase()
    }

    fun requestNavigationToLogin() {
        viewModelScope.launch {
            _loginNavigationEventLiveData.postValue(
                LoginParams(
                    projectId = authStore.signedInProjectId,
                    userId = authStore.signedInUserId ?: recentUserActivityManager.getRecentUserActivity().lastUserUsed,
                ),
            )
        }
    }

    fun handleLoginResult(result: LoginResult) {
        if (result.isSuccess) {
            forceEventSync()
        }
    }

    // initial actions

    private fun startInitialSyncIfRequired() {
        viewModelScope.launch {
            val eventSyncState = eventSyncStateFlow
                .dropWhile { it.isUninitialized() }
                .firstOrNull()
            val isRunning = eventSyncState?.isSyncRunning() ?: false
            val lastUpdate = eventSyncState?.lastSyncTime

            val isForceEventSync = when {
                isPreLogoutUpSync -> true
                configRepository.isModuleSelectionRequired() -> false
                isRunning -> false
                lastUpdate == null -> true
                timeHelper.msBetweenNowAndTime(lastUpdate) > RE_SYNC_TIMEOUT_MILLIS -> true
                else -> false
            }
            if (isForceEventSync) {
                forceEventSync(canEmitSyncButtonClick = false)
            }
        }
    }

    private fun syncImagesAfterEventsWhenRequired() {
        viewModelScope.launch {
            if (isPreLogoutUpSync) {
                eventSyncStateFlow
                    .map { it.isSyncCompleted() }
                    .distinctUntilChanged()
                    .collect { isEventSyncCompleted ->
                        if (isEventSyncCompleted) {
                            sync(SyncCommands.OneTime.Images.start())
                        }
                    }
            }
        }
    }

    private fun SyncInfo.forceEventSyncProgress() = copy(
        syncInfoSectionRecords = syncInfoSectionRecords.copy(
            counterTotalRecords = "",
            counterRecordsToUpload = "",
            counterRecordsToDownload = "",
            isInstructionDefaultVisible = false,
            isInstructionCommCarePermissionVisible = false,
            isInstructionNoModulesVisible = false,
            isInstructionOfflineVisible = false,
            isInstructionErrorVisible = false,
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
    }
}
