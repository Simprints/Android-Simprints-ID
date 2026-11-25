package com.simprints.feature.dashboard.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.sync.SyncOrchestrator
import com.simprints.infra.sync.devicestate.DeviceStateDataTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class LogoutSyncViewModel @Inject constructor(
    syncOrchestrator: SyncOrchestrator,
    authStore: AuthStore,
    deviceStateDataTracker: DeviceStateDataTracker,
) : ViewModel() {
    val logoutEventLiveData: LiveData<Unit> =
        authStore
            .observeSignedInProjectId()
            .filter { projectId -> projectId.isEmpty() }
            .distinctUntilChanged()
            .map { }
            .asLiveData(viewModelScope.coroutineContext)

    val isLogoutWithoutSyncVisibleLiveData: LiveData<Boolean> =
        syncOrchestrator
            .observeSyncState()
            .combine(deviceStateDataTracker.observeDeviceDataState()) { syncStatus, deviceDataState ->
                !syncStatus.eventSyncState.isSyncCompleted() || syncStatus.imageSyncStatus.isSyncing || deviceDataState.hasDataToUpload
            }.debounce(timeoutMillis = ANTI_JITTER_DELAY_MILLIS)
            .asLiveData(viewModelScope.coroutineContext)

    private companion object {
        private const val ANTI_JITTER_DELAY_MILLIS = 1000L
    }
}
