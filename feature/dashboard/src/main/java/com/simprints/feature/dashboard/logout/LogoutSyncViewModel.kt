package com.simprints.feature.dashboard.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class LogoutSyncViewModel @Inject constructor(
    private val configManager: ConfigManager,
    eventSyncManager: EventSyncManager,
    syncOrchestrator: SyncOrchestrator,
    authStore: AuthStore,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    val logoutEventLiveData: LiveData<Unit> =
        authStore.observeSignedInProjectId().filter { projectId ->
            projectId.isEmpty()
        }.distinctUntilChanged().map { /* Unit on every "true" */ }.asLiveData()

    val isLogoutWithoutSyncVisibleLiveData: LiveData<Boolean> = combine(
        eventSyncManager.getLastSyncState(useDefaultValue = true).asFlow(),
        syncOrchestrator.observeImageSyncStatus(),
    ) { eventSyncState, imageSyncStatus ->
        !eventSyncState.isSyncCompleted() || imageSyncStatus.isSyncing
    }.debounce(timeoutMillis = ANTI_JITTER_DELAY_MILLIS).asLiveData()

    val settingsLocked: LiveData<LiveDataEventWithContent<SettingsPasswordConfig>>
        get() = liveData(context = viewModelScope.coroutineContext) {
            emit(LiveDataEventWithContent(configManager.getProjectConfiguration().general.settingsPassword))
        }

    fun logout() {
        logoutUseCase()
    }

    private companion object {
        private const val ANTI_JITTER_DELAY_MILLIS = 1000L
    }
}
