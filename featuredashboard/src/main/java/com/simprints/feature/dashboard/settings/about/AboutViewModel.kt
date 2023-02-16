package com.simprints.feature.dashboard.settings.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.ExternalScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.config.domain.models.SettingsPasswordConfig
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AboutViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val recentUserActivityManager: RecentUserActivityManager,
    @ExternalScope private val externalScope: CoroutineScope,
) : ViewModel() {

    val syncAndSearchConfig: LiveData<SyncAndSearchConfig>
        get() = _syncAndSearchConfig
    private val _syncAndSearchConfig = MutableLiveData<SyncAndSearchConfig>()

    val modalities: LiveData<List<GeneralConfiguration.Modality>>
        get() = _modalities
    private val _modalities = MutableLiveData<List<GeneralConfiguration.Modality>>()

    val recentUserActivity: LiveData<RecentUserActivity>
        get() = _recentUserActivity
    private val _recentUserActivity = MutableLiveData<RecentUserActivity>()

    val settingsLocked: LiveData<SettingsPasswordConfig>
        get() = _settingsLocked
    private val _settingsLocked = MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    init {
        load()
    }

    fun logout() {
        externalScope.launch { signerManager.signOut() }
    }

    private fun load() = viewModelScope.launch {
        val configuration = configManager.getProjectConfiguration()
        val syncAndSearchConfig = SyncAndSearchConfig(
            configuration.synchronization.down.partitionType.name,
            configuration.identification.poolType.name,
        )
        _syncAndSearchConfig.postValue(syncAndSearchConfig)
        _modalities.postValue(configuration.general.modalities)
        _recentUserActivity.postValue(recentUserActivityManager.getRecentUserActivity())
        _settingsLocked.postValue(configuration.general.settingsPassword)
    }

}
