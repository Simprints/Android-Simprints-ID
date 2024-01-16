package com.simprints.feature.dashboard.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
) : ViewModel() {

    val generalConfiguration: LiveData<GeneralConfiguration>
        get() = _generalConfiguration
    private val _generalConfiguration = MutableLiveData<GeneralConfiguration>()

    val languagePreference: LiveData<String>
        get() = _languagePreference
    private val _languagePreference = MutableLiveData<String>()

    val settingsLocked: LiveData<SettingsPasswordConfig>
        get() = _settingsLocked
    private val _settingsLocked = MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    init {
        load()
    }

    fun updateLanguagePreference(language: String) {
        viewModelScope.launch {
            configRepository.updateDeviceConfiguration { it.apply { it.language = language } }
            _languagePreference.postValue(language)
            Simber.tag(LoggingConstants.CrashReportTag.SETTINGS.name).i("Language set to $language")
        }
    }

    private fun load() = viewModelScope.launch {
        val configuration = configRepository.getProjectConfiguration().general

        _languagePreference.postValue(configRepository.getDeviceConfiguration().language)
        _generalConfiguration.postValue(configuration)
        _settingsLocked.postValue(configuration.settingsPassword)
    }

    fun unlockSettings() {
        _settingsLocked.postValue(SettingsPasswordConfig.Unlocked)
    }
}
