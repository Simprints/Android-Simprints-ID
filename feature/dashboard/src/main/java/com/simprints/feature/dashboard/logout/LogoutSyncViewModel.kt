package com.simprints.feature.dashboard.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherMain
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.sync.SyncOrchestrator
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class LogoutSyncViewModel @Inject constructor(
    private val configManager: Lazy<ConfigManager>,
    eventSyncManager: Lazy<EventSyncManager>,
    syncOrchestrator: Lazy<SyncOrchestrator>,
    authStore: Lazy<AuthStore>,
    private val logoutUseCase: Lazy<LogoutUseCase>,
    @DispatcherMain private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val logoutEventLiveData: LiveData<Unit> =
        authStore.get()
            .observeSignedInProjectId()
            .filter { projectId ->
                projectId.isEmpty()
            }.distinctUntilChanged()
            .map { /* Unit on every "true" */ }
            .asLiveData(viewModelScope.coroutineContext)

    val isLogoutWithoutSyncVisibleLiveData: LiveData<Boolean> = flow {
        emitAll(
            combine(
                eventSyncManager.get().getLastSyncState(useDefaultValue = true).asFlow(),
                syncOrchestrator.get().observeImageSyncStatus(),
            ) { eventSyncState, imageSyncStatus ->
                !eventSyncState.isSyncCompleted() || imageSyncStatus.isSyncing
            }.debounce(timeoutMillis = ANTI_JITTER_DELAY_MILLIS)
        )
    }.flowOn(mainDispatcher).asLiveData(viewModelScope.coroutineContext)

    val settingsLocked: LiveData<LiveDataEventWithContent<SettingsPasswordConfig>>
        get() = liveData(context = viewModelScope.coroutineContext) {
            emit(LiveDataEventWithContent(configManager.get().getProjectConfiguration().general.settingsPassword))
        }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase.get().invoke()
        }
    }

    private companion object {
        private const val ANTI_JITTER_DELAY_MILLIS = 1000L
    }
}
