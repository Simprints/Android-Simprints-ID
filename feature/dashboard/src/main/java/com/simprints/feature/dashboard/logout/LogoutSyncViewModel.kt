package com.simprints.feature.dashboard.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.sync.SyncCommands
import com.simprints.infra.sync.usecase.SyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class LogoutSyncViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    sync: SyncUseCase,
    authStore: AuthStore,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    val logoutEventLiveData: LiveData<Unit> =
        authStore
            .observeSignedInProjectId()
            .filter { projectId ->
                projectId.isEmpty()
            }.distinctUntilChanged()
            .map { /* Unit on every "true" */ }
            .asLiveData(viewModelScope.coroutineContext)

    val isLogoutWithoutSyncVisibleLiveData: LiveData<Boolean> =
        sync(SyncCommands.ObserveOnly).syncStatusFlow
            .map { syncStatus ->
                !syncStatus.eventSyncState.isSyncCompleted() || syncStatus.imageSyncStatus.isSyncing
            }.debounce(timeoutMillis = ANTI_JITTER_DELAY_MILLIS)
            .asLiveData(viewModelScope.coroutineContext)

    val settingsLocked: LiveData<LiveDataEventWithContent<SettingsPasswordConfig>>
        get() = liveData(context = viewModelScope.coroutineContext) {
            emit(LiveDataEventWithContent(configRepository.getProjectConfiguration().general.settingsPassword))
        }

    fun logout() {
        logoutUseCase()
    }

    private companion object {
        private const val ANTI_JITTER_DELAY_MILLIS = 1000L
    }
}
