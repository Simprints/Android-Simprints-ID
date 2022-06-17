package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsPreferenceViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsPreferenceViewModel::class.java))
            SettingsPreferenceViewModel() as T
        else
            throw IllegalArgumentException("SettingsPreferenceViewModel not found")
    }
}
