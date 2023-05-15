package com.simprints.feature.dashboard.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.feature.dashboard.settings.about.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutSyncViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    val settingsLocked: LiveData<SettingsPasswordConfig>
        get() = _settingsLocked
    private val _settingsLocked =
        MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    init {
        viewModelScope.launch {
            val configuration = configManager.getProjectConfiguration()
            _settingsLocked.postValue(configuration.general.settingsPassword)
        }
    }

    fun logout() {
        externalScope.launch { signerManager.signOut() }
    }
}
