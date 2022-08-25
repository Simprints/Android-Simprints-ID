package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.infra.config.ConfigManager

class SettingsPreferenceViewModelFactory(private val configManager: ConfigManager) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsPreferenceViewModel::class.java))
            SettingsPreferenceViewModel(configManager) as T
        else
            throw IllegalArgumentException("SettingsPreferenceViewModel not found")
    }
}
