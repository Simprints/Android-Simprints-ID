package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsPreferenceViewModel(
    private val configManager: ConfigManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val generalConfiguration = MutableLiveData<GeneralConfiguration>()
    val languagePreference = MutableLiveData<String>()

    init {
        viewModelScope.launch(dispatcher) {
            generalConfiguration.postValue(configManager.getProjectConfiguration().general)
            languagePreference.postValue(configManager.getDeviceConfiguration().language)
        }
    }

    fun updateLanguagePreference(language: String) {
        viewModelScope.launch(dispatcher) {
            configManager.updateDeviceConfiguration { it.apply { it.language = language } }
            languagePreference.postValue(language)
            Simber.tag(CrashReportTag.SETTINGS.name).i("Language set to $language")
        }
    }
}
