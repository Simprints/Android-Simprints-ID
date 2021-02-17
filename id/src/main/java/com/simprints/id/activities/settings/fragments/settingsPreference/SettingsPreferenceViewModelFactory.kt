package com.simprints.id.activities.settings.fragments.settingsPreference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.data.analytics.crashreport.CrashReportManager

class SettingsPreferenceViewModelFactory(
    private val crashReportManager: CrashReportManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsPreferenceViewModel::class.java))
            SettingsPreferenceViewModel(crashReportManager) as T
        else
            throw IllegalArgumentException("SettingsPreferenceViewModel not found")
    }
}
