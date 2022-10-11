package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.secure.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager

class SettingsAboutViewModelFactory(
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val recentUserActivityManager: RecentUserActivityManager,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsAboutViewModel::class.java))
            SettingsAboutViewModel(configManager, signerManager, recentUserActivityManager) as T
        else
            throw IllegalArgumentException("View model not found")
    }
}
