package com.simprints.feature.dashboard.settings.syncinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.tools.time.TimeHelper
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.dashboard.settings.syncinfo.usecase.ObserveSyncInfoUseCase
import com.simprints.feature.login.LoginParams
import com.simprints.feature.login.LoginResult
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.isModuleSelectionAvailable
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SyncInfoViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val eventSyncManager: EventSyncManager,
    private val syncOrchestrator: SyncOrchestrator,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val timeHelper: TimeHelper,
    observeSyncInfo: ObserveSyncInfoUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    var isPreLogoutUpSync = false

    val loginNavigationEventLiveData: LiveData<LoginParams>
        get() = _loginNavigationEventLiveData
    private val _loginNavigationEventLiveData = MutableLiveData<LoginParams>()

    private val eventSyncStateFlow =
        eventSyncManager.getLastSyncState(useDefaultValue = true /* otherwise value not guaranteed */).asFlow()
    private val imageSyncStatusFlow =
        syncOrchestrator.observeImageSyncStatus()

    val logoutEventLiveData: LiveData<LiveDataEventWithContent<Unit>> = combine(
        eventSyncStateFlow,
        imageSyncStatusFlow,
    ) { eventSyncState, imageSyncStatus ->
        val isReadyToLogOut =
            isPreLogoutUpSync && eventSyncState.isSyncCompleted() && !imageSyncStatus.isSyncing
        return@combine isReadyToLogOut
    }.debounce(LOGOUT_DELAY_MILLIS)
        .filter { isReadyToLogOut ->
            isReadyToLogOut // only when ready
        }.map {
            LiveDataEventWithContent(Unit)
        }.asLiveData(viewModelScope.coroutineContext)

    val syncInfoLiveData: LiveData<SyncInfo> by lazy {
        observeSyncInfo(isPreLogoutUpSync)
            .onStart {
                startInitialSyncIfRequired()
                syncImagesAfterEventsWhenRequired()
            }.asLiveData(viewModelScope.coroutineContext)
    }

    fun forceEventSync() {
        viewModelScope.launch {
            syncOrchestrator.stopEventSync()
            val isDownSyncAllowed =
                !isPreLogoutUpSync && configManager.getProject(authStore.signedInProjectId).state == ProjectState.RUNNING
            syncOrchestrator.startEventSync(isDownSyncAllowed)
        }
    }

    fun toggleImageSync() {
        viewModelScope.launch {
            val isImageSyncing = imageSyncStatusFlow.firstOrNull()?.isSyncing == true
            if (isImageSyncing) {
                syncOrchestrator.stopImageSync()
            } else {
                syncOrchestrator.startImageSync()
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
            val isRunning = eventSyncManager.getLastSyncState().value?.isSyncRunning() ?: false
            val lastUpdate = eventSyncManager.getLastSyncTime()

            val isForceEventSync = when {
                isPreLogoutUpSync -> true
                configManager.isModuleSelectionRequired() -> false
                isRunning -> false
                lastUpdate == null -> true
                timeHelper.msBetweenNowAndTime(lastUpdate) > RE_SYNC_TIMEOUT_MILLIS -> true
                else -> false
            }
            if (isForceEventSync) {
                forceEventSync()
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
                            syncOrchestrator.startImageSync()
                        }
                    }
            }
        }
    }

    private suspend fun ConfigManager.isModuleSelectionRequired() =
        getProjectConfiguration().isModuleSelectionAvailable() && getDeviceConfiguration().selectedModules.isEmpty()

    private companion object {
        private const val RE_SYNC_TIMEOUT_MILLIS = 5 * 60 * 1000L
        private const val LOGOUT_DELAY_MILLIS = 3000L
    }
}
