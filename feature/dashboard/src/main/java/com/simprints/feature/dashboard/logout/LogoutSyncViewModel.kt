package com.simprints.feature.dashboard.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.infra.authlogic.AuthManager
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutSyncViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val authManager: AuthManager,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    val settingsLocked: LiveData<LiveDataEventWithContent<SettingsPasswordConfig>>
        get() = liveData(context = viewModelScope.coroutineContext) {
            emit(LiveDataEventWithContent(configRepository.getProjectConfiguration().general.settingsPassword))
        }


    fun logout() {
        externalScope.launch { authManager.signOut() }
    }
}
