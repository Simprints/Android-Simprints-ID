package com.simprints.feature.dashboard.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.sync.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class LogoutSyncViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    val settingsLocked: LiveData<LiveDataEventWithContent<SettingsPasswordConfig>>
        get() = liveData(context = viewModelScope.coroutineContext) {
            emit(LiveDataEventWithContent(configManager.getProjectConfiguration().general.settingsPassword))
        }

    fun logout() {
        logoutUseCase()
    }
}
