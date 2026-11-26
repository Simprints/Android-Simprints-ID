package com.simprints.feature.dashboard.logout.sync

import androidx.lifecycle.viewModelScope
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class LogoutSyncViewModel @Inject constructor(
    syncOrchestrator: SyncOrchestrator,
    deviceStateDataTracker: DeviceStateDataTracker,
) : BaseMviViewModel<LogoutSyncUiState, LogoutSyncUiAction, LogoutSyncUiEffect>(LogoutSyncUiState()) {
    // Stream-composed state; sources stay observed for 5 s after the last collector leaves, as before the MVI migration.
    override val uiState: StateFlow<LogoutSyncUiState> =
        syncOrchestrator
            .observeSyncState()
            .combine(deviceStateDataTracker.observeDeviceDataState()) { syncStatus, deviceDataState ->
                !syncStatus.eventSyncState.isSyncCompleted() || syncStatus.imageSyncStatus.isSyncing || deviceDataState.hasDataToUpload
            }.debounce(timeoutMillis = ANTI_JITTER_DELAY_MILLIS)
            .map { isLogoutWithoutSyncVisible -> LogoutSyncUiState(isLogoutWithoutSyncVisible = isLogoutWithoutSyncVisible) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LogoutSyncUiState())

    // LogoutSyncUiAction has no actions; the queue is never used by this screen
    override suspend fun handleAction(action: LogoutSyncUiAction) = Unit

    private companion object {
        private const val ANTI_JITTER_DELAY_MILLIS = 1000L
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
