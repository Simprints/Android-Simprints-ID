package com.simprints.feature.dashboard.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.experimental
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.sync.ConfigSyncCache
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SETTINGS
import com.simprints.infra.logging.Simber
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val syncOrchestrator: SyncOrchestrator,
    private val configSyncCache: ConfigSyncCache,
) : ViewModel() {
    val generalConfiguration: LiveData<GeneralConfiguration>
        get() = _generalConfiguration
    private val _generalConfiguration = MutableLiveData<GeneralConfiguration>()

    val experimentalConfiguration = configManager.watchProjectConfiguration()
        .map(ProjectConfiguration::experimental)
        .asLiveData(viewModelScope.coroutineContext)

    val languagePreference: LiveData<String>
        get() = _languagePreference
    private val _languagePreference = MutableLiveData<String>()

    val settingsLocked: LiveData<SettingsPasswordConfig>
        get() = _settingsLocked
    private val _settingsLocked = MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    val sinceConfigLastUpdated: LiveData<LiveDataEventWithContent<String>>
        get() = _sinceConfigLastUpdated
    private val _sinceConfigLastUpdated = MutableLiveData<LiveDataEventWithContent<String>>()

    val configUpdated: LiveData<LiveDataEvent>
        get() = _configUpdated
    private val _configUpdated = MutableLiveData<LiveDataEvent>()

    init {
        load()
    }

    fun updateLanguagePreference(language: String) {
        viewModelScope.launch {
            configManager.updateDeviceConfiguration { it.apply { it.language = language } }
            _languagePreference.postValue(language)
            Simber.i("Language set to $language", tag = SETTINGS)
        }
    }

    private fun load() = viewModelScope.launch {
        val configuration = configManager.getProjectConfiguration().general

        _sinceConfigLastUpdated.send(configSyncCache.sinceLastUpdateTime())
        _languagePreference.postValue(configManager.getDeviceConfiguration().language)
        _generalConfiguration.postValue(configuration)
        _settingsLocked.postValue(configuration.settingsPassword)
    }

    fun unlockSettings() {
        _settingsLocked.postValue(SettingsPasswordConfig.Unlocked)
    }

    fun scheduleConfigUpdate() {
        viewModelScope.launch {
            syncOrchestrator
                .refreshConfiguration()
                .collect {
                    _configUpdated.send()
                    _sinceConfigLastUpdated.send(configSyncCache.sinceLastUpdateTime())
                }
        }
    }
}
